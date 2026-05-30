# Kalkan Firebase Architecture

## Authentication

- Google Sign-In will use Firebase Authentication.
- Guest mode will use Firebase Anonymous Authentication.
- Every authenticated user gets a matching `users/{userId}` document.
- Super admin access is controlled by `users/{userId}.role == "SUPER_ADMIN"`.

## Firestore Collections

```text
users/{userId}
earthquakes/{earthquakeId}
emergency_statuses/{statusId}
emergency_contacts/{contactId}
families/{familyId}/members/{memberId}
announcements/{announcementId}
notifications/{notificationId}
admin_logs/{logId}
system_settings/general
```

## FCM Topics

```text
general
emergency_alerts
city_istanbul
city_ankara
```

## Setup Note

Add `app/google-services.json` from Firebase Console, then enable this line in `app/build.gradle.kts`:

```kotlin
alias(libs.plugins.google.services)
```
