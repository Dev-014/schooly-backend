# Schooly Notification System — Frontend API Integration Guide

This guide provides comprehensive, production-ready documentation for integrating the **Schooly Multi-Role Notification System** into `schooly-web` (React / TypeScript / TanStack Query).

---

## 1. Architecture & Protocol Overview

### 1.1 Base URL & Environment
- **Development Server**: `http://localhost:8080` (Direct) or `http://localhost:3000/api` (via Vite proxy)
- **Base Endpoint**: `/api/notifications`
- **Protocol**: RESTful JSON over HTTPS / HTTP

### 1.2 Authentication & Request Headers
All notification endpoints require an active JWT access token. The authenticated user and school context are automatically resolved by the backend.

```http
Authorization: Bearer <ACCESS_TOKEN>
Content-Type: application/json
Accept: application/json
```

### 1.3 Multi-Tenant Resolution
If the token contains `schoolId`, the backend defaults to that school. You can also explicitly pass `?schoolId={schoolId}` in query parameters when a Super Admin or multi-school manager switches schools.

### 1.4 Response Envelope Format
All responses adhere to the standard `ApiResponse<T>` contract:

```typescript
interface ApiResponse<T> {
  status: "success" | "error";
  data: T | null;
  message: string;
  pagination?: {
    page: number;
    size: number;
    total: number;
  } | null;
}
```

---

## 2. TypeScript Data Contracts & Enums

Add these definitions to your frontend types (e.g., `src/types/notification.ts`):

```typescript
export type UserRole = "SUPER_ADMIN" | "ADMIN" | "TEACHER" | "STUDENT" | "PARENT" | "STAFF";

export type NotificationCategory =
  | "ACADEMIC"
  | "ATTENDANCE"
  | "FINANCE"
  | "HR"
  | "FRONTOFFICE"
  | "LIBRARY"
  | "COMMUNICATION"
  | "SYSTEM"
  | "GENERAL";

export type NotificationPriority = "LOW" | "NORMAL" | "HIGH" | "URGENT";

export type NotificationType =
  // Academic & Homework
  | "HOMEWORK_ASSIGNED"
  | "HOMEWORK_SUBMITTED"
  | "HOMEWORK_GRADED"
  | "EXAM_SCHEDULE_PUBLISHED"
  | "EXAM_ADMIT_CARD_READY"
  | "EXAM_RESULT_PUBLISHED"
  // Attendance
  | "ATTENDANCE_ABSENT"
  | "ATTENDANCE_LATE"
  | "ATTENDANCE_LOW_WARNING"
  // Finance & Fees
  | "FEE_INVOICE_GENERATED"
  | "FEE_DUE_REMINDER"
  | "FEE_PAYMENT_SUCCESS"
  | "FEE_OVERDUE_ALERT"
  // HR & Staff
  | "LEAVE_REQUESTED"
  | "LEAVE_STATUS_UPDATED"
  | "PAYROLL_GENERATED"
  | "STAFF_TASK_ASSIGNED"
  // Front Office
  | "VISITOR_CHECKED_IN"
  | "GATE_PASS_ISSUED"
  | "PARCEL_RECEIVED"
  | "COMPLAINT_FILED"
  | "COMPLAINT_RESOLVED"
  // Library
  | "BOOK_ISSUED"
  | "BOOK_DUE_REMINDER"
  | "BOOK_OVERDUE_ALERT"
  // Communication & System
  | "CIRCULAR_ANNOUNCEMENT"
  | "EMERGENCY_ALERT"
  | "SYSTEM_NOTIFICATION";

export interface NotificationItem {
  id: number;
  schoolId: number;
  userId: number;
  targetRole: UserRole | string;
  title: string;
  message: string;
  notificationType: NotificationType;
  category: NotificationCategory;
  priority: NotificationPriority;
  isRead: boolean;
  readAt: string | null; // ISO 8601 string
  actorId: number | null;
  actorRole: string | null;
  actorName: string | null;
  actionUrl: string | null; // e.g., "/homework/12" or "/finance/fees"
  actionType: string | null; // "NAVIGATE" | "MODAL" | "EXTERNAL"
  entityType: string | null;
  entityId: number | null;
  metadata: Record<string, any>;
  createdAt: string; // ISO 8601 string
  relativeTime: string; // e.g. "just now", "15m ago", "2h ago", "yesterday"
}

export interface NotificationCountSummary {
  unreadCount: number;
  totalCount: number;
  unreadByCategory: Record<NotificationCategory | string, number>;
}

export interface NotificationQueryParams {
  schoolId?: number;
  isRead?: boolean;
  category?: NotificationCategory;
  type?: NotificationType;
  priority?: NotificationPriority;
  page?: number;
  size?: number;
}

export interface CreateNotificationPayload {
  userId?: number | null; // Optional: null = broadcast to targetRole in school
  targetRole: UserRole | string;
  title: string;
  message: string;
  notificationType: NotificationType;
  category?: NotificationCategory; // Optional: auto-derived if omitted
  priority?: NotificationPriority; // Default: "NORMAL"
  actionUrl?: string;
  actionType?: string; // Default: "NAVIGATE"
  entityType?: string;
  entityId?: number;
  metadata?: Record<string, any>;
}

export interface BatchNotificationActionPayload {
  notificationIds: number[];
}
```

---

## 3. REST API Endpoints Reference

### 3.1 Get Notifications List
Retrieve a paginated, sorted list of notifications for the currently logged-in user.

- **Method**: `GET`
- **Path**: `/api/notifications`
- **Query Parameters**:
  | Param | Type | Required | Default | Description |
  | :--- | :--- | :--- | :--- | :--- |
  | `isRead` | `boolean` | No | `null` | Filter by read status: `true` (read only), `false` (unread only), or omit (all). |
  | `category` | `string` | No | `null` | Filter by category (`ACADEMIC`, `ATTENDANCE`, `FINANCE`, `HR`, `FRONTOFFICE`, `LIBRARY`, `COMMUNICATION`). |
  | `type` | `string` | No | `null` | Filter by exact notification type (e.g. `HOMEWORK_ASSIGNED`). |
  | `priority`| `string` | No | `null` | Filter by priority (`LOW`, `NORMAL`, `HIGH`, `URGENT`). |
  | `page` | `number` | No | `0` | Zero-indexed page number. |
  | `size` | `number` | No | `20` | Page size. |
  | `schoolId`| `number` | No | token | Explicit school ID override (Super Admin). |

- **Sample Request**:
  ```http
  GET /api/notifications?isRead=false&page=0&size=10 HTTP/1.1
  Host: localhost:8080
  Authorization: Bearer <TOKEN>
  ```

- **Sample Response (`200 OK`)**:
  ```json
  {
    "status": "success",
    "data": [
      {
        "id": 7,
        "schoolId": 1,
        "userId": 7,
        "targetRole": "PARENT",
        "title": "New Homework: Mathematics",
        "message": "Sarah Teacher posted new homework: Exercise 4.2 due tomorrow by 5:00 PM.",
        "notificationType": "HOMEWORK_ASSIGNED",
        "category": "ACADEMIC",
        "priority": "NORMAL",
        "isRead": false,
        "readAt": null,
        "actorId": 3,
        "actorRole": "TEACHER",
        "actorName": "Sarah Teacher",
        "actionUrl": "/homework",
        "actionType": "NAVIGATE",
        "entityType": "HOMEWORK",
        "entityId": 701,
        "metadata": {
          "subject": "Mathematics",
          "dueDate": "Tomorrow"
        },
        "createdAt": "2026-10-27T23:45:00",
        "relativeTime": "15m ago"
      }
    ],
    "message": "Notifications retrieved successfully",
    "pagination": {
      "page": 0,
      "size": 10,
      "total": 1
    }
  }
  ```

---

### 3.2 Get Unread Summary & Badge Counters
Fast lightweight endpoint for app topbars, bell badges, and notification center category filters.

- **Method**: `GET`
- **Path**: `/api/notifications/unread-count`
- **Query Parameters**:
  - `schoolId` (`number`, optional)

- **Sample Request**:
  ```http
  GET /api/notifications/unread-count HTTP/1.1
  Host: localhost:8080
  Authorization: Bearer <TOKEN>
  ```

- **Sample Response (`200 OK`)**:
  ```json
  {
    "status": "success",
    "data": {
      "unreadCount": 3,
      "totalCount": 8,
      "unreadByCategory": {
        "ACADEMIC": 2,
        "FINANCE": 1
      }
    },
    "message": "Unread count fetched successfully",
    "pagination": null
  }
  ```

---

### 3.3 Mark Single Notification as Read
Triggered when a user clicks on an unread notification card or item in the drawer.

- **Method**: `PUT`
- **Path**: `/api/notifications/{id}/read`
- **Path Parameter**: `id` (`number`, required)
- **Sample Request**:
  ```http
  PUT /api/notifications/7/read HTTP/1.1
  Host: localhost:8080
  Authorization: Bearer <TOKEN>
  ```

- **Sample Response (`200 OK`)**:
  ```json
  {
    "status": "success",
    "data": {
      "id": 7,
      "schoolId": 1,
      "userId": 7,
      "targetRole": "PARENT",
      "title": "New Homework: Mathematics",
      "message": "Sarah Teacher posted new homework: Exercise 4.2 due tomorrow by 5:00 PM.",
      "notificationType": "HOMEWORK_ASSIGNED",
      "category": "ACADEMIC",
      "priority": "NORMAL",
      "isRead": true,
      "readAt": "2026-10-28T00:55:12",
      "actorId": 3,
      "actorRole": "TEACHER",
      "actorName": "Sarah Teacher",
      "actionUrl": "/homework",
      "actionType": "NAVIGATE",
      "entityType": "HOMEWORK",
      "entityId": 701,
      "metadata": {
        "subject": "Mathematics",
        "dueDate": "Tomorrow"
      },
      "createdAt": "2026-10-27T23:45:00",
      "relativeTime": "1h ago"
    },
    "message": "Notification marked as read",
    "pagination": null
  }
  ```

---

### 3.4 Mark All Notifications as Read
Triggered by clicking the "Mark all as read" button in the notification panel header.

- **Method**: `PUT`
- **Path**: `/api/notifications/read-all`
- **Query Parameters**: `schoolId` (`number`, optional)

- **Sample Request**:
  ```http
  PUT /api/notifications/read-all HTTP/1.1
  Host: localhost:8080
  Authorization: Bearer <TOKEN>
  ```

- **Sample Response (`200 OK`)**:
  ```json
  {
    "status": "success",
    "data": {
      "updatedCount": 3
    },
    "message": "All notifications marked as read",
    "pagination": null
  }
  ```

---

### 3.5 Batch Mark Notifications as Read
Triggered when marking a multi-selected list of items.

- **Method**: `PUT`
- **Path**: `/api/notifications/batch-read`
- **Request Body**:
  ```json
  {
    "notificationIds": [1, 2, 3]
  }
  ```

- **Sample Response (`200 OK`)**:
  ```json
  {
    "status": "success",
    "data": {
      "updatedCount": 3
    },
    "message": "Batch notifications marked as read",
    "pagination": null
  }
  ```

---

### 3.6 Dismiss / Delete Single Notification
Dismisses a single notification card from the user's feed.

- **Method**: `DELETE`
- **Path**: `/api/notifications/{id}`
- **Sample Request**:
  ```http
  DELETE /api/notifications/7 HTTP/1.1
  Host: localhost:8080
  Authorization: Bearer <TOKEN>
  ```

- **Sample Response (`200 OK`)**:
  ```json
  {
    "status": "success",
    "data": null,
    "message": "Notification dismissed successfully",
    "pagination": null
  }
  ```

---

### 3.7 Clear All Read Notifications
Removes all read notifications to clean up history and free up UI clutter.

- **Method**: `DELETE`
- **Path**: `/api/notifications/clear-read`
- **Sample Request**:
  ```http
  DELETE /api/notifications/clear-read HTTP/1.1
  Host: localhost:8080
  Authorization: Bearer <TOKEN>
  ```

- **Sample Response (`200 OK`)**:
  ```json
  {
    "status": "success",
    "data": {
      "deletedCount": 5
    },
    "message": "Read notifications cleared successfully",
    "pagination": null
  }
  ```

---

### 3.8 Dispatch Notification (Direct User or Role Broadcast)
Used by Admin or Staff UI forms to manually dispatch alerts or announcements.

- **Method**: `POST`
- **Path**: `/api/notifications`
- **Request Body Parameters**:
  | Field | Type | Required | Description |
  | :--- | :--- | :--- | :--- |
  | `targetRole` | `string` | **Yes** | Target role: `"ADMIN"`, `"TEACHER"`, `"PARENT"`, `"STAFF"`, `"STUDENT"` |
  | `userId` | `number` | No | Direct recipient ID. **If omitted, broadcasts to all active users with `targetRole` in the school!** |
  | `title` | `string` | **Yes** | Catchy headline (max 255 chars) |
  | `message` | `string` | **Yes** | Concise body description |
  | `notificationType` | `string` | **Yes** | Valid `NotificationType` enum |
  | `category` | `string` | No | Auto-derived from `notificationType` if omitted |
  | `priority` | `string` | No | `"LOW"`, `"NORMAL"`, `"HIGH"`, or `"URGENT"` (default: `"NORMAL"`) |
  | `actionUrl` | `string` | No | Frontend route path (e.g. `"/homework/45"`) |
  | `actionType` | `string` | No | `"NAVIGATE"` (default), `"MODAL"`, `"EXTERNAL"` |
  | `entityType` | `string` | No | Domain entity name (e.g. `"FEE_INVOICE"`) |
  | `entityId` | `number` | No | Entity primary key |
  | `metadata` | `object` | No | Additional custom JSON payload |

- **Sample Request (Role Broadcast)**:
  ```json
  {
    "targetRole": "PARENT",
    "title": "Severe Weather Warning: Early Dismissal",
    "message": "Due to heavy rains, school will dismiss at 1:30 PM today. School buses will depart early.",
    "notificationType": "EMERGENCY_ALERT",
    "category": "COMMUNICATION",
    "priority": "URGENT",
    "actionUrl": "/communication/circulars",
    "metadata": {
      "dismissalTime": "1:30 PM",
      "transportActive": true
    }
  }
  ```

- **Sample Response (`201 Created`)**:
  ```json
  {
    "status": "success",
    "data": [
      {
        "id": 15,
        "schoolId": 1,
        "userId": 7,
        "targetRole": "PARENT",
        "title": "Severe Weather Warning: Early Dismissal",
        "message": "Due to heavy rains, school will dismiss at 1:30 PM today. School buses will depart early.",
        "notificationType": "EMERGENCY_ALERT",
        "category": "COMMUNICATION",
        "priority": "URGENT",
        "isRead": false,
        "readAt": null,
        "actorId": 2,
        "actorRole": "ADMIN",
        "actorName": "John Admin",
        "actionUrl": "/communication/circulars",
        "actionType": "NAVIGATE",
        "entityType": null,
        "entityId": null,
        "metadata": {
          "dismissalTime": "1:30 PM",
          "transportActive": true
        },
        "createdAt": "2026-10-28T00:58:00",
        "relativeTime": "just now"
      }
    ],
    "message": "Notification(s) dispatched successfully",
    "pagination": null
  }
  ```

---

## 4. Frontend Integration Recipes (React & TanStack Query)

### 4.1 Axios / Fetch API Service (`src/services/notificationService.ts`)

```typescript
import axios from "axios";
import {
  ApiResponse,
  NotificationItem,
  NotificationCountSummary,
  NotificationQueryParams,
  CreateNotificationPayload,
  BatchNotificationActionPayload,
} from "../types/notification";

const API_BASE = "/api/notifications";

export const notificationService = {
  // Fetch paginated notification list
  getNotifications: async (params?: NotificationQueryParams): Promise<ApiResponse<NotificationItem[]>> => {
    const response = await axios.get<ApiResponse<NotificationItem[]>>(API_BASE, { params });
    return response.data;
  },

  // Fetch unread count & category stats
  getUnreadSummary: async (schoolId?: number): Promise<NotificationCountSummary> => {
    const response = await axios.get<ApiResponse<NotificationCountSummary>>(`${API_BASE}/unread-count`, {
      params: schoolId ? { schoolId } : undefined,
    });
    return response.data.data!;
  },

  // Mark single as read
  markAsRead: async (id: number): Promise<NotificationItem> => {
    const response = await axios.put<ApiResponse<NotificationItem>>(`${API_BASE}/${id}/read`);
    return response.data.data!;
  },

  // Mark all as read
  markAllAsRead: async (schoolId?: number): Promise<number> => {
    const response = await axios.put<ApiResponse<{ updatedCount: number }>>(`${API_BASE}/read-all`, null, {
      params: schoolId ? { schoolId } : undefined,
    });
    return response.data.data?.updatedCount || 0;
  },

  // Mark batch as read
  markBatchAsRead: async (ids: number[]): Promise<number> => {
    const payload: BatchNotificationActionPayload = { notificationIds: ids };
    const response = await axios.put<ApiResponse<{ updatedCount: number }>>(`${API_BASE}/batch-read`, payload);
    return response.data.data?.updatedCount || 0;
  },

  // Dismiss a notification
  dismissNotification: async (id: number): Promise<void> => {
    await axios.delete(`${API_BASE}/${id}`);
  },

  // Clear all read notifications
  clearReadNotifications: async (schoolId?: number): Promise<number> => {
    const response = await axios.delete<ApiResponse<{ deletedCount: number }>>(`${API_BASE}/clear-read`, {
      params: schoolId ? { schoolId } : undefined,
    });
    return response.data.data?.deletedCount || 0;
  },

  // Dispatch new notification (Admin/Staff)
  createNotification: async (payload: CreateNotificationPayload): Promise<NotificationItem[]> => {
    const response = await axios.post<ApiResponse<NotificationItem[]>>(API_BASE, payload);
    return response.data.data || [];
  },
};
```

---

### 4.2 TanStack Query Custom Hooks (`src/hooks/useNotifications.ts`)

```typescript
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { notificationService } from "../services/notificationService";
import { NotificationQueryParams, CreateNotificationPayload } from "../types/notification";

export const NOTIFICATION_KEYS = {
  all: ["notifications"] as const,
  list: (params?: NotificationQueryParams) => ["notifications", "list", params] as const,
  unreadCount: (schoolId?: number) => ["notifications", "unread-count", schoolId] as const,
};

// Hook: Get notifications list
export function useNotifications(params?: NotificationQueryParams) {
  return useQuery({
    queryKey: NOTIFICATION_KEYS.list(params),
    queryFn: () => notificationService.getNotifications(params),
    staleTime: 1000 * 30, // 30 seconds
  });
}

// Hook: Get real-time unread count (polls every 30s)
export function useNotificationUnreadCount(schoolId?: number) {
  return useQuery({
    queryKey: NOTIFICATION_KEYS.unreadCount(schoolId),
    queryFn: () => notificationService.getUnreadSummary(schoolId),
    refetchInterval: 30000, // 30-second heartbeat polling
    staleTime: 15000,
  });
}

// Hook: Mark single notification as read
export function useMarkAsRead() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => notificationService.markAsRead(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: NOTIFICATION_KEYS.all });
    },
  });
}

// Hook: Mark all notifications as read
export function useMarkAllAsRead() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (schoolId?: number) => notificationService.markAllAsRead(schoolId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: NOTIFICATION_KEYS.all });
    },
  });
}

// Hook: Dismiss notification
export function useDismissNotification() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => notificationService.dismissNotification(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: NOTIFICATION_KEYS.all });
    },
  });
}

// Hook: Clear read notifications
export function useClearReadNotifications() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (schoolId?: number) => notificationService.clearReadNotifications(schoolId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: NOTIFICATION_KEYS.all });
    },
  });
}
```

---

### 4.3 UI Component: Bell Icon & Animated Badge

```tsx
import React from "react";
import { Bell } from "lucide-react";
import { useNotificationUnreadCount } from "../hooks/useNotifications";

interface NotificationBellProps {
  onClick: () => void;
  isOpen: boolean;
}

export const NotificationBell: React.FC<NotificationBellProps> = ({ onClick, isOpen }) => {
  const { data: summary } = useNotificationUnreadCount();
  const unreadCount = summary?.unreadCount || 0;

  return (
    <button
      onClick={onClick}
      className="relative p-2 rounded-full hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
      aria-label="View notifications"
    >
      <Bell className={`w-5 h-5 ${isOpen ? "text-primary" : "text-slate-600 dark:text-slate-300"}`} />
      {unreadCount > 0 && (
        <span className="absolute top-1 right-1 flex items-center justify-center min-w-[18px] h-[18px] px-1 text-xs font-bold text-white bg-red-500 rounded-full animate-pulse shadow-sm">
          {unreadCount > 99 ? "99+" : unreadCount}
        </span>
      )}
    </button>
  );
};
```

---

### 4.4 UI Component: Notification Drawer Card with Navigation

```tsx
import React from "react";
import { useNavigate } from "react-router-dom";
import { CheckCircle2, Clock, AlertTriangle, BookOpen, DollarSign, Calendar, Info, X } from "lucide-react";
import { NotificationItem, NotificationCategory, NotificationPriority } from "../types/notification";
import { useMarkAsRead, useDismissNotification } from "../hooks/useNotifications";

const CategoryIcon: React.FC<{ category: NotificationCategory }> = ({ category }) => {
  switch (category) {
    case "ACADEMIC":
      return <BookOpen className="w-4 h-4 text-blue-500" />;
    case "FINANCE":
      return <DollarSign className="w-4 h-4 text-emerald-500" />;
    case "ATTENDANCE":
      return <Calendar className="w-4 h-4 text-amber-500" />;
    case "HR":
      return <Info className="w-4 h-4 text-purple-500" />;
    case "FRONTOFFICE":
      return <Clock className="w-4 h-4 text-teal-500" />;
    default:
      return <AlertTriangle className="w-4 h-4 text-slate-500" />;
  }
};

export const NotificationCard: React.FC<{ item: NotificationItem; onCloseDrawer?: () => void }> = ({
  item,
  onCloseDrawer,
}) => {
  const navigate = useNavigate();
  const markAsRead = useMarkAsRead();
  const dismiss = useDismissNotification();

  const handleClick = () => {
    if (!item.isRead) {
      markAsRead.mutate(item.id);
    }
    if (item.actionUrl) {
      navigate(item.actionUrl);
      onCloseDrawer?.();
    }
  };

  const handleDismiss = (e: React.MouseEvent) => {
    e.stopPropagation();
    dismiss.mutate(item.id);
  };

  return (
    <div
      onClick={handleClick}
      className={`group relative flex gap-3 p-3.5 rounded-xl border transition-all cursor-pointer ${
        item.isRead
          ? "bg-white dark:bg-slate-900 border-slate-100 dark:border-slate-800 opacity-80"
          : "bg-blue-50/60 dark:bg-blue-950/20 border-blue-100 dark:border-blue-900 shadow-sm"
      }`}
    >
      <div className="mt-0.5 shrink-0 p-2 rounded-lg bg-slate-100 dark:bg-slate-800">
        <CategoryIcon category={item.category} />
      </div>

      <div className="flex-1 min-w-0 pr-6">
        <div className="flex items-center gap-2 mb-0.5">
          <h4 className="text-sm font-semibold text-slate-900 dark:text-slate-100 truncate">{item.title}</h4>
          {!item.isRead && <span className="w-2 h-2 rounded-full bg-blue-600 shrink-0" />}
        </div>

        <p className="text-xs text-slate-600 dark:text-slate-300 line-clamp-2 mb-1.5 leading-relaxed">
          {item.message}
        </p>

        <div className="flex items-center gap-2 text-[11px] text-slate-400">
          <span>{item.relativeTime}</span>
          {item.actorName && (
            <>
              <span>•</span>
              <span className="font-medium text-slate-500 dark:text-slate-400 truncate">
                by {item.actorName} ({item.actorRole})
              </span>
            </>
          )}
        </div>
      </div>

      <button
        onClick={handleDismiss}
        className="absolute top-3 right-3 opacity-0 group-hover:opacity-100 text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 transition-opacity p-1 rounded"
        title="Dismiss notification"
      >
        <X className="w-3.5 h-3.5" />
      </button>
    </div>
  );
};
```

---

## 5. cURL Verification Suite

Here is a ready-to-run cURL cheat sheet to test all 8 endpoints directly in terminal:

### 1. Get All Unread Notifications
```bash
curl -X GET "http://localhost:8080/api/notifications?isRead=false&page=0&size=10" \
  -H "Authorization: Bearer <TOKEN>"
```

### 2. Get Unread Count Summary
```bash
curl -X GET "http://localhost:8080/api/notifications/unread-count" \
  -H "Authorization: Bearer <TOKEN>"
```

### 3. Mark Notification as Read
```bash
curl -X PUT "http://localhost:8080/api/notifications/1/read" \
  -H "Authorization: Bearer <TOKEN>"
```

### 4. Mark All Notifications as Read
```bash
curl -X PUT "http://localhost:8080/api/notifications/read-all" \
  -H "Authorization: Bearer <TOKEN>"
```

### 5. Batch Mark Notifications as Read
```bash
curl -X PUT "http://localhost:8080/api/notifications/batch-read" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"notificationIds": [1, 2, 3]}'
```

### 6. Dismiss a Notification
```bash
curl -X DELETE "http://localhost:8080/api/notifications/1" \
  -H "Authorization: Bearer <TOKEN>"
```

### 7. Clear All Read Notifications
```bash
curl -X DELETE "http://localhost:8080/api/notifications/clear-read" \
  -H "Authorization: Bearer <TOKEN>"
```

### 8. Send / Dispatch Notification (Role Broadcast)
```bash
curl -X POST "http://localhost:8080/api/notifications" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "targetRole": "PARENT",
    "title": "Severe Weather Early Dismissal",
    "message": "Classes will dismiss at 1:30 PM due to weather warnings.",
    "notificationType": "EMERGENCY_ALERT",
    "category": "COMMUNICATION",
    "priority": "URGENT",
    "actionUrl": "/communication/circulars"
  }'
```

---

## 6. HTTP Status & Error Codes Reference

| HTTP Status | Condition | Example Response Body |
| :--- | :--- | :--- |
| `200 OK` | Successful query or update | `{"status": "success", "data": {...}, "message": "..."}` |
| `201 CREATED`| Notification successfully dispatched | `{"status": "success", "data": [...], "message": "..."}` |
| `400 BAD REQUEST` | Validation failed (missing title, invalid enum, empty batch IDs) | `{"status": "error", "message": "Validation failed: title must not be blank"}` |
| `401 UNAUTHORIZED` | Missing or expired JWT token | `{"status": "error", "message": "Invalid or expired token"}` |
| `403 FORBIDDEN` | School suspended or insufficient role permissions | `{"status": "error", "message": "SCHOOL_SUSPENDED"}` |
| `404 NOT FOUND` | Notification ID does not exist or belongs to another user/school | `{"status": "error", "message": "Notification not found with id: 999"}` |
| `500 INTERNAL ERROR` | Database connection error | `{"status": "error", "message": "Internal server error"}` |
