package com.school.erp.service.finance;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import com.school.erp.entity.finance.Payment;
import com.school.erp.entity.finance.SchoolAccount;
import com.school.erp.entity.student.Student;
import com.school.erp.entity.superadmin.School;
import com.school.erp.entity.superadmin.SchoolSubscription;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Slf4j
public class RazorpayGatewayService {

    @Value("${app.payment.gateway.enabled:true}")
    private boolean gatewayEnabled;

    @Value("${app.payment.gateway.sandbox-mode:true}")
    private boolean sandboxMode;

    @Value("${app.payment.razorpay.key-id:rzp_test_placeholder_key}")
    private String keyId;

    @Value("${app.payment.razorpay.key-secret:placeholder_secret}")
    private String keySecret;

    @Value("${app.payment.razorpay.webhook-secret:placeholder_webhook_secret}")
    private String webhookSecret;

    @Value("${app.payment.razorpay.merchant-name:Schooly Platform}")
    private String merchantName;

    public String getKeyId() {
        return keyId;
    }

    public boolean isSandbox() {
        return sandboxMode || isPlaceholderKey(keyId);
    }

    private boolean isPlaceholderKey(String key) {
        return key == null || key.isBlank() || key.contains("placeholder");
    }

    private RazorpayClient getClient() throws RazorpayException {
        return new RazorpayClient(keyId, keySecret);
    }

    /**
     * Creates a Razorpay Order for student fee payment with Route transfer to the school's linked account.
     */
    public String createStudentFeeOrder(Payment payment, Student student, School school, String schoolRazorpayAccountId) {
        long amountInPaise = payment.getAmount().multiply(BigDecimal.valueOf(100)).longValue();

        if (isSandbox()) {
            String mockOrderId = "order_sim_" + UUID.randomUUID().toString().replace("-", "").substring(0, 14);
            log.info("[SANDBOX] Generated simulated Razorpay order {} for student payment id={}, school={} (Amount: ₹{}, Route Account: {})",
                    mockOrderId, payment.getId(), school.getName(), payment.getAmount(), schoolRazorpayAccountId);
            return mockOrderId;
        }

        try {
            RazorpayClient client = getClient();
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", payment.getReceiptNumber());

            JSONObject notes = new JSONObject();
            notes.put("paymentId", String.valueOf(payment.getId()));
            notes.put("studentId", String.valueOf(student.getId()));
            notes.put("studentName", student.getName() != null ? student.getName() : student.getFirstName());
            notes.put("schoolId", String.valueOf(school.getId()));
            notes.put("domain", "STUDENT_FEE");
            orderRequest.put("notes", notes);

            // Razorpay Route: Direct transfer to School's Linked Account
            if (schoolRazorpayAccountId != null && !schoolRazorpayAccountId.isBlank()) {
                JSONArray transfers = new JSONArray();
                JSONObject transfer = new JSONObject();
                transfer.put("account", schoolRazorpayAccountId.trim());
                transfer.put("amount", amountInPaise);
                transfer.put("currency", "INR");
                transfers.put(transfer);
                orderRequest.put("transfers", transfers);
                log.info("Attaching Razorpay Route transfer of {} paise to school account {}", amountInPaise, schoolRazorpayAccountId);
            }

            Order order = client.orders.create(orderRequest);
            return order.get("id");
        } catch (RazorpayException e) {
            log.error("Failed to create Razorpay Order for payment id={}: {}", payment.getId(), e.getMessage(), e);
            throw new RuntimeException("Payment Gateway Error: Unable to initiate order. " + e.getMessage());
        }
    }

    /**
     * Creates a Razorpay Order for School SaaS Subscription (routed to Schooly platform account).
     */
    public String createSubscriptionOrder(SchoolSubscription subscription, School school) {
        long amountInPaise = subscription.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue();

        if (isSandbox()) {
            String mockOrderId = "order_sub_sim_" + UUID.randomUUID().toString().replace("-", "").substring(0, 14);
            log.info("[SANDBOX] Generated simulated Razorpay order {} for school subscription id={}, school={} (Amount: ₹{})",
                    mockOrderId, subscription.getId(), school.getName(), subscription.getTotalAmount());
            return mockOrderId;
        }

        try {
            RazorpayClient client = getClient();
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "SUB-" + subscription.getId());

            JSONObject notes = new JSONObject();
            notes.put("subscriptionId", String.valueOf(subscription.getId()));
            notes.put("schoolId", String.valueOf(school.getId()));
            notes.put("schoolName", school.getName());
            notes.put("domain", "SCHOOL_SUBSCRIPTION");
            orderRequest.put("notes", notes);

            Order order = client.orders.create(orderRequest);
            return order.get("id");
        } catch (RazorpayException e) {
            log.error("Failed to create Razorpay Order for subscription id={}: {}", subscription.getId(), e.getMessage(), e);
            throw new RuntimeException("Subscription Gateway Error: " + e.getMessage());
        }
    }

    /**
     * Verifies payment signature (HMAC-SHA256).
     */
    public boolean verifySignature(String orderId, String paymentId, String signature) {
        if (isSandbox()) {
            log.info("[SANDBOX] Auto-validating simulated signature for orderId={}, paymentId={}", orderId, paymentId);
            return true;
        }

        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", orderId);
            options.put("razorpay_payment_id", paymentId);
            options.put("razorpay_signature", signature);

            return Utils.verifyPaymentSignature(options, keySecret);
        } catch (RazorpayException e) {
            log.error("Signature verification failed for orderId={}, paymentId={}: {}", orderId, paymentId, e.getMessage());
            return false;
        }
    }

    /**
     * Verifies incoming webhook signature.
     */
    public boolean verifyWebhookSignature(String payload, String signature) {
        if (isSandbox() && (signature == null || signature.contains("mock"))) {
            return true;
        }

        try {
            return Utils.verifyWebhookSignature(payload, signature, webhookSecret);
        } catch (RazorpayException e) {
            log.error("Webhook signature verification failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Creates a Linked Account under Schooly's Master Account for Razorpay Route.
     */
    public String createRouteLinkedAccount(School school, SchoolAccount account) {
        if (isSandbox()) {
            String mockAccountId = "acc_sim_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
            log.info("[SANDBOX] Created simulated Route Linked Account {} for school={}", mockAccountId, school.getName());
            return mockAccountId;
        }

        try {
            RazorpayClient client = getClient();
            JSONObject accountRequest = new JSONObject();
            accountRequest.put("type", "route");
            accountRequest.put("name", account.getAccountHolderName() != null ? account.getAccountHolderName() : school.getName());
            accountRequest.put("email", school.getContactEmail() != null ? school.getContactEmail() : "admin@school.com");
            accountRequest.put("tnc_accepted", true);

            JSONObject profile = new JSONObject();
            profile.put("category", "educational_institutions");
            accountRequest.put("profile", profile);

            // Razorpay accounts API (Route linked account)
            com.razorpay.Account razorpayAccount = client.account.create(accountRequest);
            return razorpayAccount.get("id");
        } catch (RazorpayException e) {
            log.error("Failed to create Razorpay Route Linked Account for school {}: {}", school.getId(), e.getMessage(), e);
            throw new RuntimeException("Route Account Creation Failed: " + e.getMessage());
        }
    }
}
