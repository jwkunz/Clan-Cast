# Requirements: Private Android Family SMS Broadcast App

## 1. Product Summary

Build a private Android application for creating and sending controlled SMS broadcasts to family contacts. The app is for personal/private sideload use only and will not be published on Google Play.

The application must support:

- Reading contacts from the device Contacts Provider.
- Creating reusable distribution lists from selected contacts and selected phone numbers.
- Creating reusable message templates with simple token replacement.
- Running a blocking preflight validation before sending.
- Rendering each recipient message before launch and persisting the rendered result.
- Sending SMS messages sequentially through a foreground service.
- Supporting pause, resume, and cancel while a broadcast is running.
- Recording detailed per-recipient send results.

The primary domain term for a mass-text operation is **Broadcast**.

## 2. Non-Goals

The application must not include the following in the MVP:

- Google Play distribution.
- Cloud backend.
- Multi-user accounts.
- Server-side messaging.
- MMS.
- RCS.
- Delivery receipt tracking.
- Automatic retry logic during a running broadcast.
- User-defined custom metadata fields.
- Advanced templating logic such as conditionals, loops, fallbacks, or branching.
- Global opt-out or suppression list.
- Automatic background campaigns without explicit user launch.

## 3. Platform Requirements

### 3.1 Android Version

- Minimum supported Android version: **Android 8.0 / API 26**.
- Target SDK may be current stable, but implementation must preserve compatibility with API 26.

### 3.2 Distribution

- Distribution method: **direct APK sideload only**.
- No Google Play release configuration is required.
- No Play Store policy workflow is required for MVP.
- Use a locally signed debug or release build.
- Updates are manual: user installs a newer APK when needed.

### 3.3 Android APIs

Use official Android APIs:

- Contacts Provider for reading device contacts.
- `android.telephony.SmsManager` for SMS sending.
- `SmsManager.sendTextMessage(...)` for single-part messages.
- `SmsManager.sendMultipartTextMessage(...)` for multi-part messages.
- Foreground service for active broadcast execution.

Relevant Android documentation:

- SmsManager: https://developer.android.com/reference/android/telephony/SmsManager
- Permissions overview: https://developer.android.com/guide/topics/permissions/overview
- Foreground services overview: https://developer.android.com/develop/background-work/services/fgs
- Foreground service types: https://developer.android.com/develop/background-work/services/fgs/service-types

## 4. Permissions

### 4.1 Manifest Permissions

The application requires:

```xml
<uses-permission android:name="android.permission.READ_CONTACTS" />
<uses-permission android:name="android.permission.SEND_SMS" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
```

If targeting Android 14+ / API 34+, declare the appropriate foreground service type and associated permission if required by the target SDK and service type.

### 4.2 Runtime Permissions

The app must request runtime permission for:

- Contacts access.
- SMS sending.

The app must handle denial gracefully:

- If contacts permission is denied, contact import and group creation cannot proceed.
- If SMS permission is denied, broadcast execution cannot proceed.
- The UI must explain the missing permission and provide a route to request again or open settings.

## 5. Core Concepts

## 5.1 Contact

A contact is sourced from the Android Contacts Provider.

The app reads contact identity and phone numbers from the device. The app does not become the canonical source of contact truth.

However, when contacts are added to a distribution list, the app stores a local record of selected metadata and the selected phone number for that group membership.

## 5.2 Distribution List

A distribution list is an app-defined group of recipients.

Requirements:

- User can create, rename, and delete distribution lists.
- User can add contacts to a distribution list.
- If a contact has multiple phone numbers, the user must explicitly select the phone number to use for that distribution list membership.
- A contact may appear in multiple distribution lists.
- A contact may appear multiple times if the user selects different numbers, subject to preflight duplicate detection.

## 5.3 Fixed Contact Metadata Fields

The app supports a fixed metadata schema only.

Required fields:

| Field | Source | Required? | Notes |
|---|---|---:|---|
| `firstName` | Parsed or derived from device contact | Yes for templates using `{firstName}` | May be edited/overridden locally if parsing is wrong. |
| `lastName` | Parsed or derived from device contact | Yes for templates using `{lastName}` | May be empty if unknown. |
| `displayName` | Device contact display name | Yes | Fallback display only; not an automatic template fallback. |
| `phoneNumber` | Selected phone number | Yes | Stored normalized where possible. |
| `relationship` | App-local manual value | Optional | Fixed field, not custom. |
| `nickname` | App-local manual value | Optional | Fixed field, not custom. |

No user-defined metadata fields are allowed in MVP.

## 5.4 Message Template

A message template is a reusable named SMS body.

Requirements:

- User can create, edit, and delete templates.
- Template fields use simple token syntax: `{fieldName}`.
- Supported tokens are exactly the fixed metadata fields.
- No conditionals.
- No loops.
- No fallback expressions.
- No computed fields.

Example:

```text
Hi {firstName}, reminder that dinner is Sunday at 5.
```

## 5.5 Broadcast

A Broadcast is a specific send operation created from:

- One distribution list.
- One message body, either typed manually or selected from a saved template.
- A snapshot of recipients.
- Pre-rendered final messages.

Broadcast names are auto-generated only.

Format:

```text
Broadcast - YYYY-MM-DD HH:mm
```

The user does not manually name Broadcasts in MVP.

## 6. Contact and Group Requirements

### 6.1 Reading Contacts

The app must read contacts from the device when permission is granted.

The contact browser must show:

- Display name.
- Available phone numbers.
- Phone number type, if available, such as mobile, home, work, or other.

### 6.2 Multi-Number Contacts

If a contact has more than one phone number:

- The app must require the user to select the specific number when adding the contact to a distribution list.
- The selected number is stored in the distribution list membership.
- The app must not automatically send to all numbers for a contact.

### 6.3 Distribution List Membership Storage

Each membership must store:

- Distribution list ID.
- Device contact ID.
- Device phone number ID, if available.
- Display name at time of adding.
- Selected phone number.
- Normalized phone number, if normalization succeeds.
- Fixed metadata fields used for templating.

### 6.4 Contact Metadata Editing

The app must allow the user to edit app-local fixed metadata fields for a distribution list member:

- `firstName`
- `lastName`
- `relationship`
- `nickname`

Editing these fields does not modify the Android device contact.

## 7. Template Requirements

### 7.1 Simple Token Replacement

Template rendering is plain token replacement.

Rules:

- `{firstName}` is replaced with the recipient snapshot field `firstName`.
- `{lastName}` is replaced with `lastName`.
- `{displayName}` is replaced with `displayName`.
- `{phoneNumber}` is replaced with `phoneNumber`.
- `{relationship}` is replaced with `relationship`.
- `{nickname}` is replaced with `nickname`.

Unsupported tokens must be treated as preflight errors.

### 7.2 Missing Values

If a template references a field that is missing or blank for any recipient:

- Preflight must report the affected recipient and field.
- Broadcast launch must be blocked until resolved.
- The app must not silently replace missing values with empty strings.
- The app must not automatically fall back to `displayName`.

### 7.3 Rendering Time

All recipient messages are rendered during preflight, before sending begins.

The rendered final message is persisted per broadcast recipient.

After a Broadcast is launched:

- The send service uses only the stored rendered message.
- The template engine is not invoked during the send loop.
- Later contact or template edits do not change the Broadcast.

## 8. Broadcast Creation Requirements

### 8.1 Snapshot Model

Broadcasts are snapshot-based.

When a Broadcast is created:

- The selected distribution list is resolved into a fixed recipient list.
- The selected metadata values are copied into the Broadcast recipient records.
- The selected phone numbers are copied into the Broadcast recipient records.
- The final message text is rendered and copied into each Broadcast recipient record.

After creation:

- The Broadcast recipient list is immutable.
- Device contact changes do not affect the Broadcast.
- Distribution list changes do not affect the Broadcast.
- Template changes do not affect the Broadcast.

### 8.2 Preflight Required

Every Broadcast must run a blocking preflight validation before launch.

The user must not be able to start sending until preflight succeeds or required decisions are explicitly made.

## 9. Preflight Validation Requirements

### 9.1 Preflight Inputs

Preflight input:

- Selected distribution list.
- Message body or selected message template.
- Current distribution list member metadata.
- Selected phone numbers.

### 9.2 Preflight Output

Preflight output screen must show:

- Total recipient entries.
- Unique phone number count.
- Duplicate phone number warnings.
- Invalid phone number warnings.
- Missing template field errors.
- Unsupported template token errors.
- Final resolved message preview per recipient.

### 9.3 Missing Template Fields

Missing template fields are blocking errors.

The user must fix the missing field values or edit the message/template before launch.

### 9.4 Unsupported Template Tokens

Unsupported template tokens are blocking errors.

Example: `{graduationYear}` is invalid because custom fields are not supported.

### 9.5 Duplicate Phone Numbers

Duplicate phone numbers are user-decision warnings.

The preflight screen must show:

- Duplicate normalized phone number.
- All affected contact entries.
- Count of duplicate entries.

The user must choose one of:

1. Proceed with deduplication applied.
2. Proceed without deduplication.
3. Cancel and edit the distribution list.

If the user chooses deduplication:

- Only one recipient entry per normalized phone number is included in the launched Broadcast.
- The UI should clearly indicate which entry will be retained.

### 9.6 Invalid Phone Numbers

Invalid phone numbers are user-decision warnings.

The preflight screen must show:

- Affected recipient.
- Original selected phone number.
- Reason if known.

The user must choose one of:

1. Exclude invalid numbers and proceed.
2. Include invalid numbers anyway.
3. Cancel and edit the distribution list.

### 9.7 Final Confirmation

Before launch, the user must confirm:

- Broadcast recipient count.
- Whether duplicates were deduped or retained.
- Whether invalid numbers were excluded or retained.
- SMS permission is granted.
- The app will send individual SMS messages sequentially.

## 10. SMS Sending Requirements

### 10.1 Sending Mode

Broadcast sending must be:

- Strictly sequential.
- One recipient at a time.
- Single-threaded queue semantics.
- Fixed delay between recipients.

Default delay:

```text
3 seconds between send attempts
```

### 10.2 Maximum Recipients

MVP maximum recipients per Broadcast:

```text
50 recipients
```

The app should block or warn if the preflight result exceeds this value. For MVP, treat this as a blocking cap unless explicitly changed later.

### 10.3 Multipart SMS

Long messages must be allowed automatically.

Implementation:

- Use `SmsManager.divideMessage(message)` to determine parts when needed.
- Use `SmsManager.sendMultipartTextMessage(...)` for multipart messages.
- Use `SmsManager.sendTextMessage(...)` for single-part messages if appropriate.

UI may show SMS segment count for information, but multipart messages are not blocked.

### 10.4 Send Status Tracking

Track sent status only.

Requirements:

- Register sent `PendingIntent` callbacks.
- Persist whether each recipient was handed off successfully or failed.
- Do not request or track delivery receipts.

### 10.5 Failure Behavior

On individual recipient failure:

- Continue to the next recipient.
- Log the failure.
- Store error code/message where available.
- Do not automatically retry during the running Broadcast.

Manual retry may be a future feature but is not part of MVP.

## 11. Foreground Service Requirements

### 11.1 Execution Environment

Broadcast execution must run in a foreground service.

Requirements:

- Service starts when a Broadcast begins.
- Service displays a persistent notification while sending.
- Notification should show progress such as `12 of 38 sent`.
- Notification should reflect paused, running, completed, or cancelled status where feasible.

### 11.2 Service Responsibilities

The foreground service owns:

- Loading pending broadcast recipients.
- Sending messages sequentially.
- Applying the fixed delay.
- Responding to pause/resume/cancel commands.
- Persisting recipient status after each send attempt.
- Updating Broadcast status.
- Updating notification progress.

### 11.3 State Persistence

The service must persist progress after each recipient.

If the app UI is closed:

- The service continues sending.
- User can return to the app and view current progress.

If the process is killed:

- MVP may leave the Broadcast in an interrupted state.
- The data model should still make it possible to resume later by finding recipients with `PENDING` status.

## 12. Broadcast Lifecycle Requirements

### 12.1 States

Broadcast states:

```text
PENDING
RUNNING
PAUSED
CANCELLED
COMPLETED
FAILED
```

`FAILED` is reserved for unrecoverable Broadcast-level errors, such as missing SMS permission after launch or service startup failure.

### 12.2 State Machine

Allowed transitions:

```text
PENDING -> RUNNING
RUNNING -> PAUSED
PAUSED -> RUNNING
RUNNING -> CANCELLED
PAUSED -> CANCELLED
RUNNING -> COMPLETED
RUNNING -> FAILED
PAUSED -> FAILED
```

Disallowed transitions:

- `COMPLETED -> RUNNING`
- `CANCELLED -> RUNNING`
- `FAILED -> RUNNING` in MVP

### 12.3 Pause

Pause behavior:

- If a message is currently being handed to `SmsManager`, allow that attempt to finish.
- Pause before the next recipient.
- Persist Broadcast state as `PAUSED`.

### 12.4 Resume

Resume behavior:

- Continue from recipients whose status is `PENDING`.
- Do not resend recipients already marked `SENT` or `FAILED`.
- Persist Broadcast state as `RUNNING`.

### 12.5 Cancel

Cancel behavior:

- Stop before the next unsent recipient.
- Persist Broadcast state as `CANCELLED`.
- Leave already-sent recipients unchanged.
- Leave unsent recipients as `PENDING` or mark them as `CANCELLED`, but choose one representation consistently.

Recommendation: mark unsent recipients as `CANCELLED` for audit clarity.

## 13. History and Audit Logging Requirements

### 13.1 Broadcast History

The app must store detailed Broadcast history.

History list must show:

- Auto-generated Broadcast name.
- Created timestamp.
- Status.
- Total recipients.
- Sent count.
- Failed count.
- Cancelled count, if applicable.

### 13.2 Per-Recipient Logs

Each Broadcast detail view must show per recipient:

- Contact display name.
- Phone number.
- Final message text.
- Status.
- Sent timestamp, if sent.
- Error code/message, if failed.

### 13.3 Audit Integrity

The app must preserve exactly what it attempted to send.

For every recipient record, persist:

- Phone number used.
- Final rendered message.
- Status.
- Timestamp.

## 14. Data Model Requirements

Use Room for local persistence.

### 14.1 Entity: `distribution_lists`

Fields:

```kotlin
id: String // UUID
name: String
createdAt: Long
updatedAt: Long
```

### 14.2 Entity: `distribution_list_members`

Fields:

```kotlin
id: String // UUID
listId: String
contactId: String
phoneNumberId: String?
displayName: String
firstName: String?
lastName: String?
relationship: String?
nickname: String?
originalPhoneNumber: String
normalizedPhoneNumber: String?
phoneNumberType: String?
createdAt: Long
updatedAt: Long
```

### 14.3 Entity: `message_templates`

Fields:

```kotlin
id: String // UUID
name: String
body: String
createdAt: Long
updatedAt: Long
```

### 14.4 Entity: `broadcasts`

Fields:

```kotlin
id: String // UUID
name: String
sourceListId: String
sourceTemplateId: String?
rawMessageBody: String
status: BroadcastStatus
totalRecipients: Int
sentCount: Int
failedCount: Int
cancelledCount: Int
createdAt: Long
startedAt: Long?
completedAt: Long?
updatedAt: Long
preflightDuplicatePolicy: DuplicatePolicy
preflightInvalidNumberPolicy: InvalidNumberPolicy
```

### 14.5 Entity: `broadcast_recipients`

Fields:

```kotlin
id: String // UUID
broadcastId: String
sourceMemberId: String?
contactId: String?
contactDisplayName: String
firstName: String?
lastName: String?
relationship: String?
nickname: String?
originalPhoneNumber: String
normalizedPhoneNumber: String?
finalMessage: String
smsPartCount: Int
status: RecipientStatus
errorCode: String?
errorMessage: String?
sentAt: Long?
createdAt: Long
updatedAt: Long
```

### 14.6 Enums

```kotlin
enum class BroadcastStatus {
    PENDING,
    RUNNING,
    PAUSED,
    CANCELLED,
    COMPLETED,
    FAILED
}

enum class RecipientStatus {
    PENDING,
    SENT,
    FAILED,
    CANCELLED
}

enum class DuplicatePolicy {
    NONE,
    DEDUPE,
    ALLOW_DUPLICATES
}

enum class InvalidNumberPolicy {
    NONE,
    EXCLUDE_INVALID,
    INCLUDE_INVALID
}
```

## 15. UI Requirements

### 15.1 Home Screen

Must provide navigation to:

- Distribution lists.
- Templates.
- New Broadcast.
- Broadcast history.

### 15.2 Distribution Lists Screen

Must support:

- List distribution lists.
- Create list.
- Rename list.
- Delete list.
- Open list detail.

### 15.3 Distribution List Detail Screen

Must support:

- View members.
- Add contact.
- Remove member.
- Edit member metadata.
- Show selected phone number.

### 15.4 Contact Picker Screen

Must support:

- Search contacts.
- Show contacts with phone numbers.
- For multi-number contacts, require explicit number selection.
- Add selected contact/number to distribution list.

### 15.5 Templates Screen

Must support:

- List templates.
- Create template.
- Edit template.
- Delete template.

### 15.6 Compose Broadcast Screen

Must support:

- Select distribution list.
- Select reusable template or enter custom message.
- Show supported template tokens.
- Continue to preflight.

### 15.7 Preflight Screen

Must support:

- Blocking validation summary.
- Recipient count.
- Duplicate issue list.
- Invalid number issue list.
- Missing field issue list.
- Unsupported token issue list.
- Per-recipient message preview.
- Explicit decisions for duplicates and invalid numbers.
- Launch Broadcast button only when blocking errors are resolved and required decisions are made.

### 15.8 Broadcast Progress Screen

Must support:

- Current Broadcast status.
- Sent / failed / pending counts.
- Progress indicator.
- Pause button while running.
- Resume button while paused.
- Cancel button while running or paused.
- Per-recipient result list.

### 15.9 History Screen

Must support:

- List previous Broadcasts.
- Show status and counts.
- Open Broadcast detail.

### 15.10 Broadcast Detail Screen

Must support:

- Broadcast metadata.
- Per-recipient audit log.
- Final rendered messages.
- Error details.

## 16. Architecture Requirements

### 16.1 Technology Stack

Use:

- Kotlin.
- Jetpack Compose.
- Room.
- Kotlin coroutines.
- Flow or StateFlow for reactive UI state.
- Foreground service for Broadcast execution.
- Repository pattern for data access.

### 16.2 Suggested Package Structure

```text
app/src/main/java/<package>/
  data/
    db/
    dao/
    entity/
    repository/
  domain/
    model/
    usecase/
    validation/
    template/
    sms/
  service/
  ui/
    home/
    lists/
    contacts/
    templates/
    compose/
    preflight/
    progress/
    history/
  util/
```

### 16.3 Domain Services

Required domain components:

- `ContactReader`
- `PhoneNumberNormalizer`
- `TemplateParser`
- `TemplateRenderer`
- `PreflightValidator`
- `BroadcastCreator`
- `SmsSender`
- `BroadcastRunner`
- `BroadcastStateController`

## 17. Error Handling Requirements

### 17.1 Permission Errors

If permissions are missing:

- Show actionable error.
- Do not crash.
- Do not start Broadcast sending.

### 17.2 SMS Send Errors

Capture result codes from sent `PendingIntent` where possible.

Persist:

- Error code.
- Human-readable mapped message if available.
- Timestamp.

### 17.3 Data Errors

Handle empty states:

- No contacts permission.
- No contacts found.
- Empty distribution list.
- No templates.
- Empty message.

Empty message must be blocked before preflight launch.

## 18. Privacy and Safety Requirements

The app is for private family use.

Safety guardrails:

- User must explicitly start each Broadcast.
- Preflight confirmation is required.
- Broadcasts are visible in history.
- No hidden sending.
- No remote trigger.
- No background autonomous scheduling.
- Sequential rate-limited sending.
- Maximum recipient cap in MVP.

## 19. Acceptance Criteria

### 19.1 MVP Acceptance Criteria

The MVP is complete when the user can:

1. Install the APK by sideloading.
2. Grant contacts and SMS permissions.
3. Create a distribution list.
4. Add contacts to the list, selecting one number per contact.
5. Edit fixed metadata fields for list members.
6. Create a reusable message template.
7. Create a Broadcast from a list and template.
8. Run preflight validation.
9. Resolve or decide duplicate and invalid number warnings.
10. Be blocked by missing field or unsupported token errors.
11. Launch a Broadcast after successful preflight.
12. Send SMS messages sequentially with 3-second delay.
13. Pause, resume, and cancel a running Broadcast.
14. See sent/failed status per recipient.
15. View detailed Broadcast history after completion.

