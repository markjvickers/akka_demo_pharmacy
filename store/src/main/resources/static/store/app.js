// React-based Patient Management System for Pharmacy Store
const { useState, useEffect, useCallback } = React;

// API utility functions
const api = {
  async request(url, options = {}) {
    try {
      const response = await fetch(url, {
        headers: {
          "Content-Type": "application/json",
          ...options.headers,
        },
        ...options,
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`HTTP ${response.status}: ${errorText}`);
      }

      const contentType = response.headers.get("content-type");
      if (contentType && contentType.includes("application/json")) {
        return await response.json();
      }
      return await response.text();
    } catch (error) {
      console.error("API Error:", error);
      throw error;
    }
  },

  // Patient CRUD operations
  async getPatient(patientId) {
    return this.request(`/patients/${patientId}`);
  },

  async createPatient(patientData) {
    return this.request("/patients/patient", {
      method: "PUT",
      body: JSON.stringify(patientData),
    });
  },

  async updatePatient(patientId, patientData) {
    return this.request(`/patients/patient/${patientId}`, {
      method: "PUT",
      body: JSON.stringify(patientData),
    });
  },

  async deletePatient(patientId) {
    return this.request(`/patients/patient/${patientId}`, {
      method: "DELETE",
    });
  },

  async searchPatients(searchParams) {
    const params = new URLSearchParams();
    if (searchParams.firstName)
      params.append("firstName", searchParams.firstName);
    if (searchParams.lastName) params.append("lastName", searchParams.lastName);
    if (searchParams.searchTerm)
      params.append("searchTerm", searchParams.searchTerm);
    return this.request(`/patients/search`, {
      method: "POST",
      body: JSON.stringify({
        firstName: searchParams.firstName,
        lastName: searchParams.lastName,
        searchTerm: searchParams.searchTerm,
      }),
    });
  },

  async mergePatients(mergeData) {
    return this.request("/patients/patient/merge", {
      method: "POST",
      body: JSON.stringify(mergeData),
    });
  },

  async getDeliverySummary() {
    return this.request("/patients/delivery/summary");
  },
};

// Alert component
function Alert({ type, children, onClose }) {
  const iconMap = {
    success: "fas fa-check-circle",
    error: "fas fa-exclamation-circle",
    warning: "fas fa-exclamation-triangle",
    info: "fas fa-info-circle",
  };

  return (
    <div className={`alert alert-${type}`}>
      <i className={iconMap[type]}></i>
      <span>{children}</span>
      {onClose && (
        <button
          onClick={onClose}
          style={{
            marginLeft: "auto",
            background: "none",
            border: "none",
            cursor: "pointer",
          }}
        >
          <i className="fas fa-times"></i>
        </button>
      )}
    </div>
  );
}

// Modal component
function Modal({ isOpen, onClose, title, children }) {
  if (!isOpen) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2 className="modal-title">{title}</h2>
          <button className="modal-close" onClick={onClose}>
            <i className="fas fa-times"></i>
          </button>
        </div>
        <div className="modal-content">{children}</div>
      </div>
    </div>
  );
}

// Patient form component
function PatientForm({ patient, onSubmit, onCancel }) {
  const [formData, setFormData] = useState({
    firstName: "",
    lastName: "",
    prefName: "",
    dateOfBirth: "",
    phoneNumber: "",
    provHealthNumber: "",
    unitNumber: "",
    streetNumber: "",
    streetName: "",
    city: "",
    province: "",
    postalCode: "",
    country: "Canada",
    langPref: "en",
    smsOptInPref: false,
    ...patient,
  });

  const [errors, setErrors] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  const validateForm = () => {
    const newErrors = {};

    if (!formData.firstName.trim())
      newErrors.firstName = "First name is required";
    if (!formData.lastName.trim()) newErrors.lastName = "Last name is required";
    if (!formData.dateOfBirth)
      newErrors.dateOfBirth = "Date of birth is required";
    if (!formData.phoneNumber.trim())
      newErrors.phoneNumber = "Phone number is required";
    if (!formData.provHealthNumber.trim())
      newErrors.provHealthNumber = "Provincial health number is required";
    if (!formData.streetNumber.trim())
      newErrors.streetNumber = "Street number is required";
    if (!formData.streetName.trim())
      newErrors.streetName = "Street name is required";
    if (!formData.city.trim()) newErrors.city = "City is required";
    if (!formData.province.trim()) newErrors.province = "Province is required";
    if (!formData.postalCode.trim())
      newErrors.postalCode = "Postal code is required";

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateForm()) return;

    setIsSubmitting(true);
    try {
      // Convert optional fields to proper format
      const submitData = {
        ...formData,
        prefName: formData.prefName ? formData.prefName : undefined,
        unitNumber: formData.unitNumber ? formData.unitNumber : undefined,
      };

      await onSubmit(submitData);
    } catch (error) {
      console.error("Submit error:", error);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleChange = (field) => (e) => {
    const value =
      e.target.type === "checkbox" ? e.target.checked : e.target.value;
    setFormData((prev) => ({ ...prev, [field]: value }));
    if (errors[field]) {
      setErrors((prev) => ({ ...prev, [field]: null }));
    }
  };

  return (
    <form onSubmit={handleSubmit} className="form">
      <div className="form-row">
        <div className="form-group">
          <label className="form-label required">First Name</label>
          <input
            type="text"
            className="form-input"
            value={formData.firstName}
            onChange={handleChange("firstName")}
            required
          />
          {errors.firstName && (
            <span className="text-error text-sm">{errors.firstName}</span>
          )}
        </div>
        <div className="form-group">
          <label className="form-label required">Last Name</label>
          <input
            type="text"
            className="form-input"
            value={formData.lastName}
            onChange={handleChange("lastName")}
            required
          />
          {errors.lastName && (
            <span className="text-error text-sm">{errors.lastName}</span>
          )}
        </div>
      </div>

      <div className="form-row">
        <div className="form-group">
          <label className="form-label">Preferred Name</label>
          <input
            type="text"
            className="form-input"
            value={formData.prefName}
            onChange={handleChange("prefName")}
          />
        </div>
        <div className="form-group">
          <label className="form-label required">Date of Birth</label>
          <input
            type="date"
            className="form-input"
            value={formData.dateOfBirth}
            onChange={handleChange("dateOfBirth")}
            required
          />
          {errors.dateOfBirth && (
            <span className="text-error text-sm">{errors.dateOfBirth}</span>
          )}
        </div>
      </div>

      <div className="form-row">
        <div className="form-group">
          <label className="form-label required">Phone Number</label>
          <input
            type="tel"
            className="form-input"
            value={formData.phoneNumber}
            onChange={handleChange("phoneNumber")}
            required
          />
          {errors.phoneNumber && (
            <span className="text-error text-sm">{errors.phoneNumber}</span>
          )}
        </div>
        <div className="form-group">
          <label className="form-label required">
            Provincial Health Number
          </label>
          <input
            type="text"
            className="form-input"
            value={formData.provHealthNumber}
            onChange={handleChange("provHealthNumber")}
            required
          />
          {errors.provHealthNumber && (
            <span className="text-error text-sm">
              {errors.provHealthNumber}
            </span>
          )}
        </div>
      </div>

      <div className="form-row-three">
        <div className="form-group">
          <label className="form-label">Unit Number</label>
          <input
            type="text"
            className="form-input"
            value={formData.unitNumber}
            onChange={handleChange("unitNumber")}
          />
        </div>
        <div className="form-group">
          <label className="form-label required">Street Number</label>
          <input
            type="text"
            className="form-input"
            value={formData.streetNumber}
            onChange={handleChange("streetNumber")}
            required
          />
          {errors.streetNumber && (
            <span className="text-error text-sm">{errors.streetNumber}</span>
          )}
        </div>
        <div className="form-group">
          <label className="form-label required">Street Name</label>
          <input
            type="text"
            className="form-input"
            value={formData.streetName}
            onChange={handleChange("streetName")}
            required
          />
          {errors.streetName && (
            <span className="text-error text-sm">{errors.streetName}</span>
          )}
        </div>
      </div>

      <div className="form-row-three">
        <div className="form-group">
          <label className="form-label required">City</label>
          <input
            type="text"
            className="form-input"
            value={formData.city}
            onChange={handleChange("city")}
            required
          />
          {errors.city && (
            <span className="text-error text-sm">{errors.city}</span>
          )}
        </div>
        <div className="form-group">
          <label className="form-label required">Province</label>
          <select
            className="form-select"
            value={formData.province}
            onChange={handleChange("province")}
            required
          >
            <option value="">Select Province</option>
            <option value="BC">British Columbia</option>
            <option value="AB">Alberta</option>
            <option value="SK">Saskatchewan</option>
            <option value="MB">Manitoba</option>
            <option value="ON">Ontario</option>
            <option value="QC">Quebec</option>
            <option value="NB">New Brunswick</option>
            <option value="NS">Nova Scotia</option>
            <option value="PE">Prince Edward Island</option>
            <option value="NL">Newfoundland and Labrador</option>
            <option value="YT">Yukon</option>
            <option value="NT">Northwest Territories</option>
            <option value="NU">Nunavut</option>
          </select>
          {errors.province && (
            <span className="text-error text-sm">{errors.province}</span>
          )}
        </div>
        <div className="form-group">
          <label className="form-label required">Postal Code</label>
          <input
            type="text"
            className="form-input"
            value={formData.postalCode}
            onChange={handleChange("postalCode")}
            pattern="[A-Za-z]\d[A-Za-z] \d[A-Za-z]\d"
            placeholder="A1A 1A1"
            required
          />
          {errors.postalCode && (
            <span className="text-error text-sm">{errors.postalCode}</span>
          )}
        </div>
      </div>

      <div className="form-row">
        <div className="form-group">
          <label className="form-label">Country</label>
          <input
            type="text"
            className="form-input"
            value={formData.country}
            onChange={handleChange("country")}
          />
        </div>
        <div className="form-group">
          <label className="form-label">Language Preference</label>
          <select
            className="form-select"
            value={formData.langPref}
            onChange={handleChange("langPref")}
          >
            <option value="en">English</option>
            <option value="fr">French</option>
          </select>
        </div>
      </div>

      <div className="form-group">
        <div className="form-checkbox">
          <input
            type="checkbox"
            id="smsOptIn"
            checked={formData.smsOptInPref}
            onChange={handleChange("smsOptInPref")}
          />
          <label htmlFor="smsOptIn" className="form-label">
            SMS Opt-in Preference
          </label>
        </div>
      </div>

      <div className="modal-footer">
        <button type="button" onClick={onCancel} className="btn btn-secondary">
          Cancel
        </button>
        <button
          type="submit"
          disabled={isSubmitting}
          className="btn btn-primary"
        >
          {isSubmitting ? (
            <>
              <i className="fas fa-spinner fa-spin"></i>
              {patient ? "Updating..." : "Creating..."}
            </>
          ) : (
            <>
              <i className={`fas ${patient ? "fa-save" : "fa-plus"}`}></i>
              {patient ? "Update Patient" : "Create Patient"}
            </>
          )}
        </button>
      </div>
    </form>
  );
}

// Patient list item component
function PatientItem({ patient, onEdit, onDelete, onView }) {
  const fullName = `${patient.firstName} ${patient.lastName}`;
  const preferredName = patient.prefName ? ` (${patient.prefName})` : "";
  const address = `${patient.unitNumber ? patient.unitNumber + "-" : ""}${patient.streetNumber} ${patient.streetName}, ${patient.city}, ${patient.province}`;

  return (
    <div className="patient-item">
      <div className="patient-info">
        <h3>
          {fullName}
          {preferredName}
        </h3>
        <p>
          <i className="fas fa-birthday-cake"></i> {patient.dateOfBirth}
        </p>
        <p>
          <i className="fas fa-phone"></i> {patient.phoneNumber}
        </p>
        <p>
          <i className="fas fa-map-marker-alt"></i> {address}
        </p>
        <p>
          <i className="fas fa-id-card"></i> {patient.provHealthNumber}
        </p>
      </div>
      <div className="patient-actions">
        <button
          onClick={() => onView(patient)}
          className="btn btn-secondary btn-sm"
        >
          <i className="fas fa-eye"></i> View
        </button>
        <button
          onClick={() => onEdit(patient)}
          className="btn btn-primary btn-sm"
        >
          <i className="fas fa-edit"></i> Edit
        </button>
        <button
          onClick={() => onDelete(patient)}
          className="btn btn-error btn-sm"
        >
          <i className="fas fa-trash"></i> Delete
        </button>
      </div>
    </div>
  );
}

// Patient detail view component
function PatientDetails({ patient, onClose, onEdit }) {
  const fullAddress = `${patient.unitNumber ? patient.unitNumber + "-" : ""}${patient.streetNumber} ${patient.streetName}, ${patient.city}, ${patient.province} ${patient.postalCode}, ${patient.country}`;

  return (
    <div className="card">
      <div className="card-header">
        <h2 className="card-title">
          {patient.firstName} {patient.lastName}
          {patient.prefName && (
            <span className="text-secondary"> ({patient.prefName})</span>
          )}
        </h2>
        <div className="flex gap-2">
          <button
            onClick={() => onEdit(patient)}
            className="btn btn-primary btn-sm"
          >
            <i className="fas fa-edit"></i> Edit
          </button>
          <button onClick={onClose} className="btn btn-secondary btn-sm">
            <i className="fas fa-times"></i> Close
          </button>
        </div>
      </div>

      <div className="form">
        <div className="form-row">
          <div className="form-group">
            <label className="form-label">Date of Birth</label>
            <p
              className="form-input"
              style={{ backgroundColor: "#f8fafc", border: "none" }}
            >
              {patient.dateOfBirth}
            </p>
          </div>
          <div className="form-group">
            <label className="form-label">Phone Number</label>
            <p
              className="form-input"
              style={{ backgroundColor: "#f8fafc", border: "none" }}
            >
              {patient.phoneNumber}
            </p>
          </div>
        </div>

        <div className="form-group">
          <label className="form-label">Provincial Health Number</label>
          <p
            className="form-input"
            style={{ backgroundColor: "#f8fafc", border: "none" }}
          >
            {patient.provHealthNumber}
          </p>
        </div>

        <div className="form-group">
          <label className="form-label">Address</label>
          <p
            className="form-input"
            style={{ backgroundColor: "#f8fafc", border: "none" }}
          >
            {fullAddress}
          </p>
        </div>

        <div className="form-row">
          <div className="form-group">
            <label className="form-label">Language Preference</label>
            <p
              className="form-input"
              style={{ backgroundColor: "#f8fafc", border: "none" }}
            >
              {patient.langPref === "en" ? "English" : "French"}
            </p>
          </div>
          <div className="form-group">
            <label className="form-label">SMS Opt-in</label>
            <p
              className="form-input"
              style={{ backgroundColor: "#f8fafc", border: "none" }}
            >
              {patient.smsOptInPref ? "Yes" : "No"}
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}

// Main App component
function App() {
  const [patients, setPatients] = useState([]);
  const [filteredPatients, setFilteredPatients] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [loading, setLoading] = useState(false);
  const [alert, setAlert] = useState(null);
  const [activeTab, setActiveTab] = useState("patients");
  const [deliverySummary, setDeliverySummary] = useState(null);

  // Modal states
  const [showPatientModal, setShowPatientModal] = useState(false);
  const [editingPatient, setEditingPatient] = useState(null);
  const [viewingPatient, setViewingPatient] = useState(null);

  const showAlert = (type, message) => {
    setAlert({ type, message });
    setTimeout(() => setAlert(null), 5000);
  };

  const searchPatients = useCallback(async () => {
    if (!searchTerm.trim() && !firstName.trim() && !lastName.trim()) {
      setFilteredPatients([]);
      return;
    }

    setLoading(true);
    try {
      const searchParams = {};
      if (firstName.trim()) searchParams.firstName = firstName.trim();
      if (lastName.trim()) searchParams.lastName = lastName.trim();
      if (searchTerm.trim()) searchParams.searchTerm = searchTerm.trim();

      const results = await api.searchPatients(searchParams);
      setFilteredPatients(Array.isArray(results) ? results : []);
    } catch (error) {
      console.error("Search error:", error);
      showAlert("error", `Search failed: ${error.message}`);
      setFilteredPatients([]);
    } finally {
      setLoading(false);
    }
  }, [searchTerm, firstName, lastName]);

  const loadDeliverySummary = useCallback(async () => {
    try {
      const summary = await api.getDeliverySummary();
      setDeliverySummary(summary);
    } catch (error) {
      console.error("Error loading delivery summary:", error);
      showAlert("error", `Failed to load delivery summary: ${error.message}`);
    }
  }, []);

  useEffect(() => {
    const delayedSearch = setTimeout(() => {
      searchPatients();
    }, 300);

    return () => clearTimeout(delayedSearch);
  }, [searchPatients]);

  useEffect(() => {
    if (activeTab === "delivery") {
      loadDeliverySummary();
    }
  }, [activeTab, loadDeliverySummary]);

  const handleCreatePatient = async (patientData) => {
    try {
      const newPatientId = await api.createPatient(patientData);
      showAlert("success", `Patient created successfully! ID: ${newPatientId}`);
      setShowPatientModal(false);
      searchPatients(); // Refresh search results
    } catch (error) {
      showAlert("error", `Failed to create patient: ${error.message}`);
    }
  };

  const handleUpdatePatient = async (patientData) => {
    try {
      await api.updatePatient(editingPatient.patientId, patientData);
      showAlert("success", "Patient updated successfully!");
      setShowPatientModal(false);
      setEditingPatient(null);
      searchPatients(); // Refresh search results
    } catch (error) {
      showAlert("error", `Failed to update patient: ${error.message}`);
    }
  };

  const handleDeletePatient = async (patient) => {
    if (
      !window.confirm(
        `Are you sure you want to delete ${patient.firstName} ${patient.lastName}?`,
      )
    ) {
      return;
    }

    try {
      await api.deletePatient(patient.patientId);
      showAlert("success", "Patient deleted successfully!");
      searchPatients(); // Refresh search results
    } catch (error) {
      showAlert("error", `Failed to delete patient: ${error.message}`);
    }
  };

  const handleEditPatient = (patient) => {
    setEditingPatient(patient);
    setShowPatientModal(true);
  };

  const handleViewPatient = (patient) => {
    setViewingPatient(patient);
  };

  const clearSearch = () => {
    setSearchTerm("");
    setFirstName("");
    setLastName("");
    setFilteredPatients([]);
  };

  return (
    <div className="app">
      <header className="header">
        <div className="header-content">
          <div className="logo">
            <i className="fas fa-pills"></i>
            Pharmacy Store - Patient Management
          </div>
          <div className="header-actions">
            <button
              onClick={() => setShowPatientModal(true)}
              className="btn btn-primary"
            >
              <i className="fas fa-plus"></i>
              Add Patient
            </button>
          </div>
        </div>
      </header>

      <nav className="nav">
        <div className="nav-content">
          <div
            className={`nav-item ${activeTab === "patients" ? "active" : ""}`}
            onClick={() => setActiveTab("patients")}
          >
            <i className="fas fa-users"></i>
            Patient Management
          </div>
          <div
            className={`nav-item ${activeTab === "delivery" ? "active" : ""}`}
            onClick={() => setActiveTab("delivery")}
          >
            <i className="fas fa-truck"></i>
            Delivery Management
          </div>
        </div>
      </nav>

      <main className="main-content">
        {alert && (
          <Alert type={alert.type} onClose={() => setAlert(null)}>
            {alert.message}
          </Alert>
        )}

        {activeTab === "patients" && (
          <>
            <div className="card">
              <div className="card-header">
                <h2 className="card-title">Search Patients</h2>
              </div>

              <div className="search-bar">
                <div className="search-input">
                  <input
                    type="text"
                    className="form-input"
                    placeholder="Search by first or last name..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                  />
                </div>
                <div className="search-input">
                  <input
                    type="text"
                    className="form-input"
                    placeholder="First name..."
                    value={firstName}
                    onChange={(e) => setFirstName(e.target.value)}
                  />
                </div>
                <div className="search-input">
                  <input
                    type="text"
                    className="form-input"
                    placeholder="Last name..."
                    value={lastName}
                    onChange={(e) => setLastName(e.target.value)}
                  />
                </div>
                <button onClick={clearSearch} className="btn btn-secondary">
                  <i className="fas fa-times"></i>
                  Clear
                </button>
              </div>
            </div>

            <div className="card">
              <div className="card-header">
                <h2 className="card-title">Search Results</h2>
                {filteredPatients.length > 0 && (
                  <span className="text-secondary">
                    {filteredPatients.length} patient(s) found
                  </span>
                )}
              </div>

              {loading ? (
                <div className="text-center">
                  <div
                    className="spinner"
                    style={{ margin: "2rem auto" }}
                  ></div>
                  <p>Searching patients...</p>
                </div>
              ) : filteredPatients.length > 0 ? (
                <div className="patient-list">
                  {filteredPatients.map((patient) => (
                    <PatientItem
                      key={patient.patientId}
                      patient={patient}
                      onEdit={handleEditPatient}
                      onDelete={handleDeletePatient}
                      onView={handleViewPatient}
                    />
                  ))}
                </div>
              ) : searchTerm || firstName || lastName ? (
                <div className="empty-state">
                  <i className="fas fa-search"></i>
                  <h3>No patients found</h3>
                  <p>Try adjusting your search criteria</p>
                </div>
              ) : (
                <div className="empty-state">
                  <i className="fas fa-users"></i>
                  <h3>Search for patients</h3>
                  <p>Enter a name to start searching</p>
                </div>
              )}
            </div>
          </>
        )}

        {activeTab === "delivery" && (
          <div className="card">
            <div className="card-header">
              <h2 className="card-title">Patient Record Delivery Summary</h2>
              <button
                onClick={loadDeliverySummary}
                className="btn btn-secondary"
              >
                <i className="fas fa-sync-alt"></i>
                Refresh
              </button>
            </div>

            {deliverySummary ? (
              <div className="stats-grid">
                <div className="stat-card">
                  <div className="stat-value">{deliverySummary.required}</div>
                  <div className="stat-label">Required Deliveries</div>
                </div>
                <div className="stat-card">
                  <div className="stat-value text-success">
                    {deliverySummary.delivered}
                  </div>
                  <div className="stat-label">Completed Deliveries</div>
                </div>
                <div className="stat-card">
                  <div className="stat-value text-warning">
                    {deliverySummary.outstanding}
                  </div>
                  <div className="stat-label">Pending Deliveries</div>
                </div>
              </div>
            ) : (
              <div className="text-center">
                <div className="spinner" style={{ margin: "2rem auto" }}></div>
                <p>Loading delivery summary...</p>
              </div>
            )}
          </div>
        )}
      </main>

      {/* Patient Modal */}
      <Modal
        isOpen={showPatientModal}
        onClose={() => {
          setShowPatientModal(false);
          setEditingPatient(null);
        }}
        title={editingPatient ? "Edit Patient" : "Add New Patient"}
      >
        <PatientForm
          patient={editingPatient}
          onSubmit={editingPatient ? handleUpdatePatient : handleCreatePatient}
          onCancel={() => {
            setShowPatientModal(false);
            setEditingPatient(null);
          }}
        />
      </Modal>

      {/* Patient Details Modal */}
      <Modal
        isOpen={!!viewingPatient}
        onClose={() => setViewingPatient(null)}
        title="Patient Details"
      >
        {viewingPatient && (
          <PatientDetails
            patient={viewingPatient}
            onClose={() => setViewingPatient(null)}
            onEdit={() => {
              setViewingPatient(null);
              handleEditPatient(viewingPatient);
            }}
          />
        )}
      </Modal>
    </div>
  );
}

// Render the app
ReactDOM.render(<App />, document.getElementById("root"));
