# Implementation Plan: Private Android Family SMS Broadcast App

## 1. Implementation Strategy

Build the app as a staged Android MVP using vertical slices. Each phase should produce a compilable app with incremental functionality.

Priorities:

1. Establish architecture and persistence.
2. Implement contact reading and distribution lists.
3. Implement templates and token rendering.
4. Implement preflight validation.
5. Implement Broadcast snapshot creation.
6. Implement foreground-service SMS sending.
7. Implement lifecycle controls.
8. Implement detailed history and audit views.

The Codex agent should keep the project buildable after every major phase.

## 2. Target Stack

Use:

- Kotlin.
- Android Gradle Plugin.
- Jetpack Compose.
- Room.
- Kotlin coroutines.
- StateFlow.
- Foreground service.
- Android Contacts Provider.
- Android `SmsManager`.

Minimum SDK:

```kotlin
minSdk = 26
```

Suggested target SDK:

```kotlin
targetSdk = current stable installed SDK
```

## 3. Project Initialization Plan

### 3.1 Create Android Project

Create a single-module Android app.

Suggested package name:

```text
com.privatefamily.broadcastsms
```

Suggested app name:

```text
Family Broadcast SMS
```

### 3.2 Configure Gradle

Add dependencies for:

- Compose BOM.
- Compose Material 3.
- Navigation Compose.
- Room runtime.
- Room compiler via KSP.
- Room Kotlin extensions.
- Kotlin coroutines Android.
- Lifecycle ViewModel Compose.

### 3.3 Configure Manifest

Add permissions:

```xml
<uses-permission android:name="android.permission.READ_CONTACTS" />
<uses-permission android:name="android.permission.SEND_SMS" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
```

Add foreground service declaration.

If targeting Android 14+ / API 34, include required foreground service type declaration and permission if needed by the selected service type.

### 3.4 Create Base App Shell

Implement:

- `MainActivity`.
- Compose app theme.
- Navigation host.
- Top-level screens with placeholder content.

Initial routes:

```text
home
lists
listDetail/{listId}
contactPicker/{listId}
templates
composeBroadcast
preflight/{draftId}
progress/{broadcastId}
history
broadcastDetail/{broadcastId}
```

## 4. Phase 1: Data Layer

### 4.1 Create Room Entities

Implement entities:

- `DistributionListEntity`
- `DistributionListMemberEntity`
- `MessageTemplateEntity`
- `BroadcastEntity`
- `BroadcastRecipientEntity`

Use string UUID primary keys.

### 4.2 Create Enums and Type Converters

Enums:

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

Create Room type converters for enum/string conversion if not using direct string fields.

### 4.3 Create DAOs

Create DAO interfaces:

- `DistributionListDao`
- `DistributionListMemberDao`
- `MessageTemplateDao`
- `BroadcastDao`
- `BroadcastRecipientDao`

Required DAO capabilities:

#### Distribution Lists

- Insert list.
- Update list name.
- Delete list.
- Observe all lists.
- Observe list by ID.

#### Distribution List Members

- Insert member.
- Update member metadata.
- Delete member.
- Observe members by list ID.
- Get members by list ID once.

#### Templates

- Insert template.
- Update template.
- Delete template.
- Observe all templates.
- Get template by ID.

#### Broadcasts

- Insert Broadcast.
- Update status.
- Update counts.
- Observe all Broadcasts.
- Observe Broadcast by ID.
- Get active Broadcast by ID.

#### Broadcast Recipients

- Insert recipients.
- Observe recipients by Broadcast ID.
- Get pending recipients by Broadcast ID.
- Update recipient status.
- Mark pending recipients as cancelled.

### 4.4 Create Database

Create `AppDatabase` with all entities and DAOs.

Set export schema to true if schema tracking is desired; otherwise false for MVP.

### 4.5 Create Repositories

Create repository classes:

- `DistributionListRepository`
- `TemplateRepository`
- `BroadcastRepository`
- `ContactRepository`

Repositories should hide DAO implementation details from ViewModels and domain use cases.

## 5. Phase 2: Permissions

### 5.1 Create Permission Helpers

Implement helpers for:

- Checking `READ_CONTACTS`.
- Requesting `READ_CONTACTS`.
- Checking `SEND_SMS`.
- Requesting `SEND_SMS`.

### 5.2 UI Permission States

Screens that require permissions must support:

- Permission granted.
- Permission denied.
- Permission permanently denied or requires settings.

### 5.3 Acceptance Tests for Phase 2

Manual checks:

- App starts without crashing before permissions are granted.
- Contact screens request contacts permission.
- Broadcast launch blocks if SMS permission is missing.

## 6. Phase 3: Contact Reading

### 6.1 Implement ContactReader

Create a `ContactReader` domain component that reads:

- Contact ID.
- Display name.
- Phone number ID.
- Phone number.
- Phone number type label.

Use Android Contacts Provider queries.

### 6.2 Contact Model

Create domain models:

```kotlin
data class DeviceContact(
    val contactId: String,
    val displayName: String,
    val phoneNumbers: List<DevicePhoneNumber>
)

data class DevicePhoneNumber(
    val phoneNumberId: String?,
    val number: String,
    val typeLabel: String?
)
```

### 6.3 Parsing Names

Implement basic parsing from display name:

- First token -> `firstName`.
- Remaining tokens -> `lastName`.
- Preserve full `displayName`.

Do not modify device contacts.

### 6.4 Phone Normalization

Implement `PhoneNumberNormalizer`.

MVP acceptable behavior:

- Strip spaces, hyphens, parentheses, and common separators.
- Preserve leading `+`.
- Treat too-short or empty numbers as invalid.

Optional later improvement:

- Add libphonenumber.

### 6.5 Contact Picker UI

Build contact picker screen.

Requirements:

- Search contacts by display name or phone number.
- Show contacts with phone numbers.
- If one number exists, allow direct add.
- If multiple numbers exist, require explicit number selection.
- Add selected contact/number to selected distribution list.

## 7. Phase 4: Distribution Lists

### 7.1 Lists Screen

Implement:

- Create list.
- Rename list.
- Delete list.
- Navigate to list detail.

### 7.2 List Detail Screen

Implement:

- Show members.
- Add member via contact picker.
- Remove member.
- Edit fixed metadata fields.

### 7.3 Member Edit UI

Fields:

- `firstName`
- `lastName`
- `relationship`
- `nickname`

Read-only display fields:

- `displayName`
- selected phone number
- phone number type

### 7.4 Acceptance Criteria for Phase 4

- User can create a list.
- User can add a contact with selected phone number.
- User can add a multi-number contact by selecting exactly one number.
- User can edit metadata.
- Data persists across app restart.

## 8. Phase 5: Templates

### 8.1 Template List Screen

Implement:

- Show templates.
- Create template.
- Edit template.
- Delete template.

### 8.2 Template Editor

Fields:

- Template name.
- Template body.

Show supported tokens:

```text
{firstName}
{lastName}
{displayName}
{phoneNumber}
{relationship}
{nickname}
```

### 8.3 Template Parser

Create `TemplateParser`.

Responsibilities:

- Extract all tokens matching `{tokenName}`.
- Return unique token names.
- Detect unsupported tokens.

Supported tokens only:

```kotlin
setOf(
    "firstName",
    "lastName",
    "displayName",
    "phoneNumber",
    "relationship",
    "nickname"
)
```

### 8.4 Template Renderer

Create `TemplateRenderer`.

Responsibilities:

- Replace supported tokens with recipient snapshot values.
- Return rendered string.
- Do not apply fallbacks.
- Do not replace missing values silently during validation path.

Recommended API:

```kotlin
data class RenderInput(
    val firstName: String?,
    val lastName: String?,
    val displayName: String,
    val phoneNumber: String,
    val relationship: String?,
    val nickname: String?
)

class TemplateRenderer {
    fun render(template: String, input: RenderInput): String
}
```

Validation should run before rendering to ensure missing fields do not become empty strings.

## 9. Phase 6: Compose Broadcast Draft

### 9.1 Compose Broadcast Screen

Implement screen with:

- Distribution list selector.
- Template selector.
- Message body editor.
- Token help display.
- Continue to preflight.

### 9.2 Draft Handling

MVP can use in-memory navigation state or a temporary draft table.

Recommendation for reliability:

Create a simple `BroadcastDraft` state object held in ViewModel until preflight creates a persisted Broadcast.

### 9.3 Validation Before Preflight

Block continuing if:

- No distribution list selected.
- Selected distribution list is empty.
- Message body is blank.

## 10. Phase 7: Preflight Engine

### 10.1 Create Preflight Domain Models

Implement:

```kotlin
data class PreflightRequest(
    val distributionListId: String,
    val messageBody: String,
    val duplicatePolicy: DuplicatePolicy?,
    val invalidNumberPolicy: InvalidNumberPolicy?
)

data class PreflightResult(
    val totalEntries: Int,
    val validEntries: List<PreflightRecipient>,
    val duplicateIssues: List<DuplicateIssue>,
    val invalidNumberIssues: List<InvalidNumberIssue>,
    val missingFieldIssues: List<MissingFieldIssue>,
    val unsupportedTokenIssues: List<String>,
    val canLaunch: Boolean
)
```

### 10.2 Implement Missing Field Detection

Algorithm:

1. Parse tokens from message body.
2. Reject unsupported tokens.
3. For each recipient, check each supported token used.
4. If the field is null or blank, create a `MissingFieldIssue`.
5. Missing field issues are blocking.

### 10.3 Implement Duplicate Detection

Algorithm:

1. Group recipients by normalized phone number if present.
2. If normalized number is absent, use original number only for display, not reliable duplicate grouping.
3. Any group with count > 1 is a duplicate issue.
4. Present all affected recipients.

### 10.4 Implement Invalid Number Detection

Algorithm:

1. Run `PhoneNumberNormalizer` for each selected number.
2. Mark invalid if blank, too short, or normalization fails.
3. Present affected recipients.

### 10.5 Preflight Policy Handling

Rules:

- Unsupported tokens block launch.
- Missing fields block launch.
- Duplicate issues require selected duplicate policy.
- Invalid numbers require selected invalid number policy.

Policies:

- Duplicate policy:
  - `DEDUPE`
  - `ALLOW_DUPLICATES`
- Invalid number policy:
  - `EXCLUDE_INVALID`
  - `INCLUDE_INVALID`

### 10.6 Message Rendering in Preflight

After blocking errors are resolved and policies are selected:

1. Filter invalid recipients if policy is `EXCLUDE_INVALID`.
2. Deduplicate recipients if policy is `DEDUPE`.
3. Render final message for each included recipient.
4. Calculate SMS part count.
5. Create persisted Broadcast and BroadcastRecipient records.

### 10.7 Preflight Screen UI

Show sections:

- Summary.
- Blocking errors.
- Duplicate warnings and policy selector.
- Invalid number warnings and policy selector.
- Message preview.

Launch button enabled only when:

- No missing fields.
- No unsupported tokens.
- Duplicate policy selected if duplicates exist.
- Invalid number policy selected if invalid numbers exist.
- Recipient count within cap.

## 11. Phase 8: Broadcast Creation

### 11.1 BroadcastCreator Use Case

Create `BroadcastCreator`.

Input:

- Final preflight result.
- Selected policies.
- Source list ID.
- Source template ID if applicable.
- Raw message body.

Output:

- Persisted Broadcast ID.

### 11.2 Broadcast Creation Steps

1. Generate UUID.
2. Generate name using current timestamp:

```text
Broadcast - YYYY-MM-DD HH:mm
```

3. Insert Broadcast with status `PENDING`.
4. Insert all BroadcastRecipient records with status `PENDING`.
5. Store rendered final message per recipient.
6. Store SMS part count per recipient.
7. Store selected duplicate and invalid number policies.

### 11.3 Recipient Cap

Before insertion, enforce max recipients:

```text
50 recipients
```

If over cap, preflight launch is blocked.

## 12. Phase 9: SMS Sending Layer

### 12.1 SmsSender Interface

Create abstraction:

```kotlin
interface SmsSender {
    suspend fun send(recipientId: String, phoneNumber: String, message: String): SmsSendResult
}

data class SmsSendResult(
    val success: Boolean,
    val errorCode: String? = null,
    val errorMessage: String? = null
)
```

### 12.2 AndroidSmsSender Implementation

Use `SmsManager`.

Behavior:

- Divide message with `SmsManager.divideMessage(message)`.
- If one part, call `sendTextMessage`.
- If multiple parts, call `sendMultipartTextMessage`.
- Use sent `PendingIntent` callbacks to determine handoff result.
- Do not use delivery intents.

### 12.3 PendingIntent Result Handling

Implement a `BroadcastReceiver` for SMS sent results.

It must map result codes into success/failure.

Store:

- Success/failure.
- Error code.
- Error message if mapped.

### 12.4 Coroutine Bridge

Because `SmsManager` uses callback-style `PendingIntent`, provide a coroutine bridge:

- Start send.
- Suspend until sent result arrives or timeout occurs.
- Return `SmsSendResult`.

Timeout should be conservative, for example 30 seconds.

If timeout occurs:

- Mark recipient as `FAILED`.
- Store error message `SMS sent callback timeout`.
- Continue to next recipient.

## 13. Phase 10: Foreground Broadcast Service

### 13.1 Service Class

Create:

```kotlin
BroadcastSendService
```

Responsibilities:

- Accept commands through intents.
- Run send loop in coroutine scope.
- Maintain foreground notification.
- Persist state transitions.

### 13.2 Service Commands

Define actions:

```kotlin
ACTION_START_BROADCAST
ACTION_PAUSE_BROADCAST
ACTION_RESUME_BROADCAST
ACTION_CANCEL_BROADCAST
```

Intent extras:

```kotlin
EXTRA_BROADCAST_ID
```

### 13.3 Notification Channel

Create notification channel:

```text
broadcast_sending
```

Notification content:

- Title: `Sending Broadcast`
- Body examples:
  - `Running: 12 of 40 processed`
  - `Paused: 12 of 40 processed`
  - `Cancelling...`

### 13.4 Send Loop

Pseudocode:

```kotlin
while (true) {
    val broadcast = repository.getBroadcast(broadcastId)

    when (broadcast.status) {
        PAUSED -> {
            delay(500)
            continue
        }
        CANCELLED -> {
            repository.markRemainingRecipientsCancelled(broadcastId)
            stopSelf()
            return
        }
        RUNNING -> Unit
        else -> return
    }

    val next = repository.getNextPendingRecipient(broadcastId)
    if (next == null) {
        repository.markBroadcastCompleted(broadcastId)
        stopSelf()
        return
    }

    val result = smsSender.send(
        recipientId = next.id,
        phoneNumber = next.normalizedPhoneNumber ?: next.originalPhoneNumber,
        message = next.finalMessage
    )

    if (result.success) {
        repository.markRecipientSent(next.id, now())
    } else {
        repository.markRecipientFailed(next.id, result.errorCode, result.errorMessage, now())
    }

    repository.refreshBroadcastCounts(broadcastId)
    updateNotification(broadcastId)
    delay(3_000)
}
```

### 13.5 Pause Handling

Pause should not interrupt an in-flight SMS handoff.

Implementation:

- Set Broadcast status to `PAUSED`.
- Send loop observes status after current send completes.
- Loop waits while paused.

### 13.6 Resume Handling

Resume implementation:

- Set Broadcast status to `RUNNING`.
- If service is not active, start service with resume/start command.
- Continue from pending recipients.

### 13.7 Cancel Handling

Cancel implementation:

- Set Broadcast status to `CANCELLED`.
- Mark remaining pending recipients as `CANCELLED`.
- Stop service after current send handoff completes.

## 14. Phase 11: Broadcast Progress UI

### 14.1 ViewModel

Create `BroadcastProgressViewModel`.

It observes:

- Broadcast entity.
- Broadcast recipients.

Derived state:

- Sent count.
- Failed count.
- Pending count.
- Cancelled count.
- Progress percentage.
- Available actions based on status.

### 14.2 Progress Screen

Show:

- Broadcast name.
- Status.
- Counts.
- Progress indicator.
- Pause/resume/cancel controls.
- Recipient result list.

### 14.3 Controls

Buttons:

- `Pause` visible when status is `RUNNING`.
- `Resume` visible when status is `PAUSED`.
- `Cancel` visible when status is `RUNNING` or `PAUSED`.

Each button sends command to service or repository/controller.

## 15. Phase 12: History UI

### 15.1 History Screen

Show all Broadcasts sorted newest first.

Each row:

- Name.
- Created timestamp.
- Status.
- Sent/failed/cancelled counts.

### 15.2 Broadcast Detail Screen

Show:

- Broadcast metadata.
- Raw message body.
- Selected policies.
- Recipient list.
- Final rendered message per recipient.
- Error code/message for failed recipients.

## 16. Phase 13: App Polish and Safety

### 16.1 Empty States

Implement clear empty states for:

- No contacts permission.
- No distribution lists.
- Empty distribution list.
- No templates.
- No Broadcast history.

### 16.2 Confirmation Copy

Before launch, display explicit confirmation:

```text
This will send individual SMS messages from this device to N recipients. Messages will be sent one at a time with a 3-second delay.
```

### 16.3 Error Messages

Use plain-language errors:

- `SMS permission is required to send a Broadcast.`
- `Contacts permission is required to create distribution lists.`
- `This template uses unsupported fields.`
- `Some recipients are missing required template fields.`

### 16.4 No Hidden Sending

Ensure there is no code path that starts Broadcast sending without:

1. User composing or selecting a message.
2. Preflight running.
3. User confirming launch.

## 17. Phase 14: Manual QA Plan

### 17.1 Permissions QA

Test cases:

1. First launch without contacts permission.
2. Deny contacts permission.
3. Grant contacts permission.
4. Deny SMS permission and attempt launch.
5. Grant SMS permission and launch.

### 17.2 Distribution List QA

Test cases:

1. Create distribution list.
2. Add contact with one phone number.
3. Add contact with multiple phone numbers.
4. Confirm number picker is required.
5. Edit metadata.
6. Remove member.
7. Delete list.

### 17.3 Template QA

Test cases:

1. Create template with `{firstName}`.
2. Create template with unsupported `{graduationYear}`.
3. Edit template.
4. Delete template.

### 17.4 Preflight QA

Test cases:

1. Empty distribution list blocks.
2. Blank message blocks.
3. Missing `{firstName}` blocks.
4. Unsupported token blocks.
5. Duplicate number shows warning.
6. Deduplication policy works.
7. Allow duplicates policy works.
8. Invalid number warning appears.
9. Exclude invalid policy works.
10. Include invalid policy works.
11. Recipient cap blocks over 50 recipients.

### 17.5 Sending QA

Use a small test group first.

Test cases:

1. Send to one recipient.
2. Send to two recipients and verify delay.
3. Send multipart SMS.
4. Simulate failure if possible.
5. Confirm failure does not stop Broadcast.
6. Confirm sent/failed status persists.

### 17.6 Lifecycle QA

Test cases:

1. Pause while running.
2. Resume after pause.
3. Cancel while running.
4. Background app while running.
5. Reopen app and verify progress state.
6. Confirm completed Broadcast appears in history.

## 18. Suggested Development Order for Codex

Give Codex tasks in this order.

### Task 1: Scaffold Project

Create Android project with Compose, Room, navigation, and app shell.

Deliverables:

- Buildable project.
- MainActivity.
- Navigation routes.
- Placeholder screens.

### Task 2: Add Room Data Layer

Implement entities, DAOs, database, repositories, and enum converters.

Deliverables:

- Room database compiles.
- Repository methods compile.
- Basic fake data optional.

### Task 3: Implement Distribution Lists

Implement list CRUD and member CRUD without contact picker first.

Deliverables:

- Create/rename/delete lists.
- Show list detail.
- Manually insert test member if needed.

### Task 4: Implement Contacts Provider Reader

Implement permission request, contact query, contact picker, and selected-number add flow.

Deliverables:

- Read contacts.
- Search contacts.
- Add selected number to list.

### Task 5: Implement Template CRUD and Parser

Implement template screens and token parser.

Deliverables:

- Create/edit/delete templates.
- Extract supported/unsupported tokens.

### Task 6: Implement Preflight Validator

Implement validation engine and preflight UI.

Deliverables:

- Missing fields block.
- Unsupported tokens block.
- Duplicates require policy.
- Invalid numbers require policy.
- Message previews render.

### Task 7: Implement Broadcast Snapshot Creation

Persist Broadcast and BroadcastRecipients after successful preflight.

Deliverables:

- Auto-generated Broadcast name.
- Final messages stored per recipient.
- Broadcast detail can display stored recipients.

### Task 8: Implement SmsSender

Implement Android SmsManager wrapper and sent result receiver.

Deliverables:

- Single SMS send works.
- Multipart SMS send works.
- Sent callback updates result.

### Task 9: Implement Foreground Service

Implement sequential send loop with 3-second delay.

Deliverables:

- Start Broadcast service.
- Send recipients one at a time.
- Persist sent/failed results.
- Notification shows progress.

### Task 10: Implement Pause/Resume/Cancel

Implement Broadcast lifecycle controls.

Deliverables:

- Pause stops before next recipient.
- Resume continues pending recipients.
- Cancel marks remaining recipients cancelled.

### Task 11: Implement History and Detail Views

Implement Broadcast history and detailed audit log.

Deliverables:

- History list.
- Broadcast detail page.
- Final rendered messages visible.
- Error details visible.

### Task 12: Polish and QA Fixes

Implement empty states, copy, error handling, and manual QA fixes.

Deliverables:

- App is usable end-to-end.
- No hidden send path.
- Permission denial handled.
- Recipient cap enforced.

## 19. Implementation Notes and Edge Cases

### 19.1 SMS Rate Limits

Android, carrier, and OEM behavior may vary. The fixed 3-second delay reduces risk but does not guarantee all messages will be accepted.

### 19.2 Sent vs Delivered

The app tracks only sent/handoff status. It does not prove recipient delivery.

### 19.3 Multipart Cost

Multipart SMS can count as multiple carrier messages. The app allows multipart automatically.

### 19.4 Contact Changes

Broadcasts are snapshots. Later device contact edits do not alter existing Broadcasts.

### 19.5 Process Death

MVP should persist enough state to avoid duplicate sends. Full robust process-death recovery can be improved later.

Minimum behavior:

- Sent recipients remain `SENT`.
- Failed recipients remain `FAILED`.
- Pending recipients remain `PENDING`.
- Resume sends only `PENDING` recipients.

## 20. Definition of Done

The implementation is done when:

- The app builds into a sideloadable APK.
- User can create distribution lists from device contacts.
- User can select one number per contact.
- User can create reusable templates.
- Preflight validates missing fields, unsupported tokens, duplicates, and invalid numbers.
- Broadcast launch creates immutable recipient snapshots with final rendered messages.
- Foreground service sends sequential SMS messages with 3-second delay.
- Sent status and failures are logged per recipient.
- Pause/resume/cancel work.
- History shows detailed per-recipient audit logs.

