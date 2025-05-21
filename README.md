# Nail Salon Android App

An Android application for a nail salon booking system, featuring separate interfaces for customers and employees. Built with Java, Volley, and Material Components.

## Features

### Customer Flow

* **Authentication**: Email/password login, signup, and password reset.
* **Service Selection**: Browse available nail services with icons and descriptions.
* **Technician Selection**: Choose from a list of nail technicians.
* **Booking**: Select date and time via a custom calendar view and time-slot grid.
* **Booking Date**: Custom date picker for booking selection.
* **Payment**: Complete payment flow with options for discounts and promotions.
* **Discounts & Offers**: View and apply available discounts and promotional codes.
* **Gallery**: Browse a gallery of nail art designs for inspiration.
* **AR Try-On**: Real-time AR nail preview using device camera, with hand landmark detection and overlay.
* **Profile Management**: View and edit user profile (username, email, password).

### Employee Flow

* **Dashboard**: View today's appointments count, daily revenue, and clients served.
* **Upcoming Appointments**: Scrollable list of scheduled appointments.
* **Profile Management**: View and edit employee profile, including photo upload.
* **Training Videos**: Play and track completion of training videos.
* **Placeholder Screens**: Appointments detail, messages, and inventory (to be implemented).

## Architecture & Technologies

* **Language**: Java
* **Network**: Volley for HTTP requests and JSON parsing
* **Image Loading**: Glide for profile images and video thumbnails
* **UI & Layout**: AndroidX, Material Components (Toolbar, BottomNavigationView, Material Buttons)
* **Custom Views**: RecyclerView, CardView, CircleImageView
* **Persistence**: SharedPreferences wrapped by `PrefManager`

## Project Structure

```
com.example.androidexample
├── MainActivity.java                  # Entry point & login logic
├── SignupActivity.java               # User registration flow
├── VerificationActivity.java         # User verification (OTP/email)
├── ResetActivity.java                # Password reset flow
├── ApplicationActivity.java          # Customer home screen
├── ARNailActivity.java               # AR-based nail try-on using MediaPipe
├── LaptopCameraActivity.java         # Handles camera input for AR
├── HandLandmarkHelper.java           # Hand landmark detection utilities
├── HandOverlayView.java              # Overlay view for AR hand landmarks
├── SelectServiceActivity.java        # Choose a nail service
├── SelectNailTechActivity.java       # Choose a technician
├── SelectBookingDate.java            # Date picker for bookings
├── SelectBookingTimeActivity.java    # Calendar & time-slot booking
├── BookingConfirmationActivity.java  # Confirm booking details
├── DiscountsActivity.java            # Display and apply discounts
├── CompletePaymentActivity.java      # Payment processing screen
├── GalleryActivity.java              # Nail art gallery for inspiration
├── ViewProfileActivity.java          # Customer profile view/edit
├── EditProfileActivity.java          # Customer profile update via API
├── ServiceItem.java                  # Model for home screen cards
├── ServiceAdapter.java               # RecyclerView adapter for ServiceItem
├── ViewMessagesActivity.java         # View message threads
│
├── ChatBotActivity.java              # Chatbot interface for customer support
├── ChatActivity.java                 # In-app messaging screen
│
├── EmployeeHomeActivity.java         # Employee dashboard
├── EmployeeAppointmentsActivity.java # Placeholder for appointments list
├── ViewAllAppointmentsActivity.java  # View all appointments
├── EmployeeAppointmentDetailActivity.java # Appointment detail view
├── EmployeeViewProfileActivity.java  # Employee profile view
├── EmployeeEditProfileActivity.java  # Employee profile update
├── EmployeeMessagesActivity.java     # Messages interface
├── ViewAllReviewsActivity.java       # View all customer reviews
├── EmployeeVideoActivity.java        # Training video player
├── EmployeeFinanceDialog.java        # Dialog for handling employee finances
├── EmployeeNotesActivity.java        # Notes interface for employees
│
├── admin
│   ├── AdminDashboardActivity.java   # Admin overview and metrics
│   ├── AllAppointmentsActivity.java  # List all appointments
│   ├── AllOffersActivity.java        # Manage promotional offers
│   ├── AllRewardsActivity.java       # Manage user rewards
│   ├── AppointmentItem.java          # Model for admin appointment list items
│   └── ViewAllServices.java          # Manage available services
│
└── utils
    ├── PrefManager.java              # SharedPreferences helper
    ├── VolleySingleton.java          # Centralized request queue (if implemented)
    ├── WebSocketListener.java        # WebSocket event listener
    └── WebSocketManager.java         # WebSocket connection manager
```

## Dependencies

Add the following to your `build.gradle` (app) dependencies:

```groovy
implementation 'com.android.volley:volley:1.2.1'
implementation 'com.github.bumptech.glide:glide:4.12.0'
implementation 'com.google.android.material:material:1.5.0'
implementation 'de.hdodenhof:circleimageview:3.1.0'
```

## Setup & Running

1. **Clone the repository**:

   ```bash
   git clone https://github.com/yourusername/nailsalon-android.git
   cd nailsalon-android
   ```
2. **Open in Android Studio** and allow Gradle to sync.
3. **Configure API endpoints**:

   * Update base URLs in activities (e.g., `SERVER_LOGINURL`, `UPDATE_URL`).
4. **Permissions**:

   * Ensure Internet permission in `AndroidManifest.xml`:

     ```xml
     <uses-permission android:name="android.permission.INTERNET" />
     ```
5. **Run** on an emulator or physical device (min. SDK version based on `build.gradle`).

## Configuration

* **Backend**: The app expects a RESTful API at specified endpoints. Ensure the server is running and reachable.
* **Server URLs**: Modify in each activity or centralize in a constants file.

## Contributing

1. Fork the repository.
2. Create a feature branch: `git checkout -b feature/YourFeature`
3. Commit your changes: `git commit -m "Add YourFeature"`
4. Push to the branch: `git push origin feature/YourFeature`
5. Open a pull request.

## License

This project is licensed under the MIT License. See `LICENSE.md` for details.
