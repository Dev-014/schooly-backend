import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TestPermissions {
    public static void main(String[] args) throws Exception {
        // Authenticate (login as super admin or school admin to get token)
        String loginJson = "{\"email\":\"admin@smartschool.edu\",\"password\":\"password\"}";
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest loginReq = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:8080/api/auth/login"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(loginJson))
            .build();
        
        HttpResponse<String> loginRes = client.send(loginReq, HttpResponse.BodyHandlers.ofString());
        System.out.println("Login: " + loginRes.statusCode());
        String token = loginRes.body().split("\"token\":\"")[1].split("\"")[0];
        
        // Fetch roles for schoolId=4
        HttpRequest rolesReq = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:8080/api/roles?schoolId=4"))
            .header("Authorization", "Bearer " + token)
            .GET()
            .build();
        HttpResponse<String> rolesRes = client.send(rolesReq, HttpResponse.BodyHandlers.ofString());
        System.out.println("Roles: " + rolesRes.body());
        
        String roleId = rolesRes.body().split("\"id\":\"")[1].split("\"")[0];
        
        // Fetch permissions for that role
        HttpRequest permsReq = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:8080/api/roles/" + roleId + "/permissions?schoolId=4"))
            .header("Authorization", "Bearer " + token)
            .GET()
            .build();
        HttpResponse<String> permsRes = client.send(permsReq, HttpResponse.BodyHandlers.ofString());
        System.out.println("Perms: " + permsRes.body());
    }
}
