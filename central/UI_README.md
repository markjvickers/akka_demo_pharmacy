# Central Pharmacy Management System - UI Documentation

## Overview

This is a modern, responsive single-page application (SPA) built for managing pharmacy and patient data in the Central Pharmacy Management System. The UI provides an intuitive interface for performing CRUD operations on pharmacies and patients, with a beautiful, interactive design featuring animations, theme switching, and real-time feedback.

## Features

### 🏥 Core Functionality
- **Pharmacy Management**: Create, read, update, and delete pharmacy records
- **Patient Management**: Full CRUD operations for patient records
- **Real-time Search**: Instant search and retrieval of pharmacy and patient data
- **Form Validation**: Comprehensive client-side validation with user feedback

### 🎨 Modern UI/UX
- **Responsive Design**: Works seamlessly on desktop, tablet, and mobile devices
- **Dark/Light Theme**: Toggle between themes with smooth transitions
- **Interactive Animations**: Hover effects, button ripples, card glows, and smooth transitions
- **Progress Indicators**: Visual feedback during API operations
- **Toast Notifications**: Contextual alerts for success, error, and info messages
- **Floating Action Button**: Quick scroll-to-top functionality

### 🚀 Technical Features
- **Single Page Application**: Fast navigation without page reloads
- **Modern JavaScript**: ES6+ features with TypeScript-style architecture
- **CSS Variables**: Consistent theming and easy customization
- **Font Awesome Icons**: Beautiful iconography throughout the interface
- **Google Fonts**: Inter font for modern typography

## Architecture

### Frontend Stack
- **HTML5**: Semantic markup with accessibility considerations
- **CSS3**: Modern features including CSS Grid, Flexbox, and Custom Properties
- **JavaScript ES6+**: Modular, class-based architecture
- **Font Awesome**: Icon library
- **Google Fonts**: Typography

### Backend Integration
- **Akka HTTP**: RESTful API endpoints
- **Static Content Serving**: Akka endpoint serves UI files
- **JSON API**: Clean REST interface for data operations

## API Endpoints

### Pharmacy Endpoints
```
GET    /pharmacies/{pharmacy_id}     - Retrieve pharmacy by ID
PUT    /pharmacies/pharmacy          - Create new pharmacy
POST   /pharmacies/pharmacy          - Update existing pharmacy
DELETE /pharmacies/{pharmacy_id}     - Delete pharmacy
```

### Patient Endpoints
```
GET    /patients/{store_patient_id}  - Retrieve patient by store ID
PUT    /patients/patient             - Create new patient
POST   /patients/patient             - Update existing patient
DELETE /patients/{store_patient_id}  - Delete patient
```

### UI Endpoints
```
GET    /                            - Serve main UI
GET    /ui                          - Serve main UI
GET    /ui/{path}                   - Serve UI (SPA routing)
GET    /static/{filename}           - Serve static assets
GET    /favicon.ico                 - Serve favicon
```

## Setup and Installation

### Prerequisites
- Java 11 or higher
- Maven 3.6+
- Modern web browser

### Building the Application
```bash
cd pharmacy/central
mvn clean compile
mvn package
```

### Running the Application
```bash
cd pharmacy/central
mvn exec:java -Dexec.mainClass="akka.javasdk.Main"
```

The application will start on `http://localhost:9001`

### Accessing the UI
Once the server is running, access the UI at:
- `http://localhost:9001/` - Main application
- `http://localhost:9001/ui` - Alternative UI route

## Usage Guide

### Pharmacy Management

#### Adding a Pharmacy
1. Navigate to the **Pharmacies** section (active by default)
2. Fill in the "Add/Update Pharmacy" form:
   - **Pharmacy ID**: Unique identifier (required)
   - **Address**: Full pharmacy address (required)
   - **Phone Number**: Contact number (required)
   - **Version**: Version identifier (defaults to "1.0")
3. Click **Add Pharmacy**
4. Success notification will appear

#### Searching for a Pharmacy
1. In the "Find Pharmacy" card, enter the **Pharmacy ID**
2. Click **Search**
3. If found, pharmacy details will appear in the results section
4. Form will auto-populate for editing

#### Updating a Pharmacy
1. Search for the pharmacy first (auto-populates form)
2. Modify the desired fields
3. Click **Update Pharmacy**

#### Deleting a Pharmacy
1. Search for the pharmacy first
2. In the results section, click the **Delete** button
3. Confirm the deletion in the popup dialog

### Patient Management

#### Adding a Patient
1. Click on the **Patients** tab in the navigation
2. Fill in the "Add/Update Patient" form with all required fields:
   - **Pharmacy ID**: Associated pharmacy (required)
   - **Patient ID**: Unique patient identifier (required)
   - **Personal Information**: Name, preferred name, date of birth
   - **Contact Information**: Phone number, provincial health number
   - **Address**: Complete address information
   - **Preferences**: Language and SMS opt-in preferences
3. Click **Add Patient**

#### Searching for a Patient
1. Enter the **Store Patient ID** in the format: `{pharmacy_id}-{patient_id}`
2. Example: `101-a0ae1e15-bb87-4a36-90fe-a0a7e63aca4a`
3. Click **Search**

#### Updating/Deleting Patients
Similar process to pharmacies - search first, then modify or delete.

## Data Models

### Pharmacy Model
```json
{
  "pharmacyId": "101",
  "address": "123 Main Street, City, Province",
  "phoneNumber": "555-123-4567",
  "version": "1.0"
}
```

### Patient Model
```json
{
  "pharmacyId": "101",
  "patientId": "uuid-patient-id",
  "firstName": "John",
  "lastName": "Doe",
  "prefName": "Johnny",
  "dateOfBirth": "1985-01-15",
  "phoneNumber": "555-987-6543",
  "provHealthNumber": "PHN123456",
  "unitNumber": "5B",
  "streetNumber": "123",
  "streetName": "Oak Street",
  "city": "Vancouver",
  "province": "BC",
  "postalCode": "V5K0A1",
  "country": "Canada",
  "langPref": "en",
  "smsOptInPref": true
}
```

## UI Components

### Theme Toggle
- **Location**: Top-right corner of the screen
- **Function**: Switches between light and dark themes
- **Persistence**: Theme preference saved in localStorage

### Progress Bar
- **Location**: Top of screen during operations
- **Function**: Shows operation progress during API calls
- **Behavior**: Automatically appears/disappears with loading states

### Floating Action Button
- **Location**: Bottom-right corner
- **Function**: Scroll to top of page
- **Visibility**: Appears when scrolled down >300px

### Alerts/Notifications
- **Location**: Top-right corner
- **Types**: Success (green), Error (red), Info (blue)
- **Behavior**: Auto-dismiss after 5 seconds, manual close available

## Customization

### CSS Variables
The UI uses CSS custom properties for easy theming:

```css
:root {
  --primary-color: #2563eb;
  --secondary-color: #10b981;
  --danger-color: #ef4444;
  /* ... more variables */
}
```

### Adding New Features
1. **API Client**: Extend the `PharmacyAPI` class
2. **UI Manager**: Add methods to the `UIManager` class
3. **HTML**: Add new sections/forms to `index.html`
4. **Styling**: Use existing CSS variables for consistency

## Browser Support

- **Chrome**: 90+
- **Firefox**: 88+
- **Safari**: 14+
- **Edge**: 90+

## Performance Considerations

- **Lazy Loading**: Static assets loaded on demand
- **Efficient Animations**: Hardware-accelerated CSS transforms
- **Minimal Dependencies**: Only external dependencies are fonts and icons
- **Optimized Bundling**: Single HTML file with inline CSS/JS for fast loading

## Troubleshooting

### Common Issues

#### UI Not Loading
- Check that the Akka server is running on port 9000
- Verify static files are in `src/main/resources/static/`
- Check browser console for JavaScript errors

#### API Calls Failing
- Confirm backend services are running
- Check network tab in browser developer tools
- Verify API endpoint URLs are correct

#### Styling Issues
- Clear browser cache
- Check for CSS conflicts in developer tools
- Verify CSS variables are properly defined

#### Form Validation Errors
- Ensure all required fields are filled
- Check field format requirements (dates, phone numbers)
- Verify special characters in IDs

### Debug Mode
Enable debug logging by adding to browser console:
```javascript
localStorage.setItem('debug', 'true');
```

## Security Considerations

- **Input Validation**: Client-side validation for user experience
- **XSS Prevention**: No innerHTML usage with user data
- **CSRF**: API endpoints should implement CSRF protection
- **HTTPS**: Use HTTPS in production environments

## Future Enhancements

### Planned Features
- **Bulk Operations**: Import/export functionality
- **Advanced Search**: Filtering and sorting capabilities
- **User Authentication**: Login/logout functionality
- **Real-time Updates**: WebSocket integration
- **Offline Support**: Service worker implementation
- **Print Support**: Formatted printing of records

### Technical Improvements
- **TypeScript**: Full TypeScript implementation
- **Build Process**: Webpack/Vite integration
- **Testing**: Unit and integration tests
- **PWA**: Progressive Web App features

## Contributing

### Code Style
- Use 2-space indentation
- Follow existing naming conventions
- Add JSDoc comments for new functions
- Maintain consistent CSS organization

### Testing
Test the UI with:
- Different screen sizes and devices
- Various data inputs including edge cases
- All CRUD operations
- Theme switching
- Error scenarios

## License

This project is part of the Central Pharmacy Management System and follows the same licensing terms.

## Contact

For technical support or questions about the UI implementation, please refer to the main project documentation or contact the development team.

---

*Last updated: August 2024*
*Version: 1.0.0*
