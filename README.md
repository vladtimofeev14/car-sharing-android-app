# Car Sharing Android App (Turo Clone)

This project is a **student Android application** developed as part of a mobile development course.  
It is a simplified clone of **Turo**, a peer-to-peer car sharing platform, built to demonstrate core Android development concepts.

The application allows users to:
- Register and log in
- List their own cars for rent (owner role)
- Browse available cars
- Book cars for a selected date range (renter role)
- Manage bookings as both renter and owner

---

## Technologies Used

- **Kotlin**
- **Android SDK**
- **Firebase Authentication**
- **Firebase Firestore**
- **Google Maps SDK for Android**

---

## Project Architecture

The project follows standard Android architecture and separation of concerns:

- **Activities**  
  Handle UI logic and lifecycle management.

- **Adapters**  
  Used with RecyclerView to display lists of data (listings, bookings).

- **Models (Data Classes)**  
  Represent Firestore documents such as `Listing` and `Booking`.

- **Firebase Services**
  - Authentication: user login and session handling
  - Firestore: listings, bookings, and user data storage

---

## Main Features

### Authentication
- Email/password authentication using Firebase Authentication
- User session handling via a dedicated `UserSession` class

### Listings (Owner)
- Owners can create car listings with:
  - Brand, model, color
  - License plate
  - Address and city
  - Price per day
  - Location shown on Google Maps
- Owners can view all their listings in **My Listings**

### Search & Map
- Renters can browse available cars
- Listings are displayed on Google Maps using markers
- Each marker represents a car location

### Bookings (Renter)
- Renters can book a car by selecting start and end dates
- Each booking generates a **confirmation code**
- Renters can view and cancel their own bookings in **My Bookings**

### Booking Management (Owner)
- Owners can view bookings made for their listings
- Owners can manage and cancel bookings

---

## Google Maps Integration

- Google Maps is used to display car locations
- Each listing includes latitude and longitude
- Markers are added dynamically based on Firestore data
- Maps are configured via Google Maps SDK and API key

> **Note:**  
> The Google Maps API key and `google-services.json` file are intentionally excluded from this repository for security reasons.

---

## Firebase Configuration

This project uses Firebase for backend services.

⚠️ **Important:**  
To run the project locally, you must:

1. Create your own Firebase project
2. Enable:
   - Firebase Authentication (Email/Password)
   - Cloud Firestore
3. Add your own `google-services.json` file
4. Add your Google Maps API key

These files are excluded via `.gitignore`.

---

## Installation & Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/vladtimofeev14/car-sharing-android-app.git
