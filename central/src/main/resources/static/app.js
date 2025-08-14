// Central Pharmacy Management System
// Modern JavaScript/TypeScript-style implementation

class PharmacyAPI {
  constructor(baseUrl = "") {
    this.baseUrl = baseUrl;
  }

  async request(endpoint, options = {}) {
    const url = `${this.baseUrl}${endpoint}`;
    const config = {
      headers: {
        "Content-Type": "application/json",
        ...options.headers,
      },
      ...options,
    };

    try {
      const response = await fetch(url, config);

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(
          `HTTP ${response.status}: ${errorText || response.statusText}`,
        );
      }

      // Handle empty responses (like DELETE operations)
      if (
        response.status === 204 ||
        response.headers.get("content-length") === "0"
      ) {
        return null;
      }

      const contentType = response.headers.get("content-type");
      if (contentType && contentType.includes("application/json")) {
        return await response.json();
      }

      return await response.text();
    } catch (error) {
      console.error("API Request failed:", error);
      throw error;
    }
  }

  // Pharmacy API methods
  async getPharmacy(pharmacyId) {
    return await this.request(`/pharmacies/${pharmacyId}`);
  }

  async addPharmacy(pharmacy) {
    return await this.request("/pharmacies/pharmacy", {
      method: "PUT",
      body: JSON.stringify(pharmacy),
    });
  }

  async updatePharmacy(pharmacy) {
    return await this.request("/pharmacies/pharmacy", {
      method: "POST",
      body: JSON.stringify(pharmacy),
    });
  }

  async deletePharmacy(pharmacyId) {
    return await this.request(`/pharmacies/${pharmacyId}`, {
      method: "DELETE",
    });
  }

  // Patient API methods
  async getPatient(storePatientId) {
    return await this.request(`/patients/${storePatientId}`);
  }

  async addPatient(patient) {
    return await this.request("/patients/patient", {
      method: "PUT",
      body: JSON.stringify(patient),
    });
  }

  async updatePatient(patient) {
    return await this.request("/patients/patient", {
      method: "POST",
      body: JSON.stringify(patient),
    });
  }

  async deletePatient(storePatientId) {
    return await this.request(`/patients/${storePatientId}`, {
      method: "DELETE",
    });
  }

  async searchPatients(searchCriteria) {
    return await this.request("/patients/search", {
      method: "POST",
      body: JSON.stringify(searchCriteria),
    });
  }
}

class UIManager {
  constructor() {
    this.currentSection = "pharmacy";
    this.currentPharmacy = null;
    this.currentPatient = null;
    this.api = new PharmacyAPI();
    this.darkMode = false;
    this.progressValue = 0;
    this.searchHistory = this.loadSearchHistory();
    this.init();
  }

  init() {
    this.bindNavigation();
    this.bindForms();
    this.bindButtons();
    this.initThemeToggle();
    this.initScrollEffects();
    this.initProgressBar();
    this.addInteractiveEffects();
    this.addAnimationStyles();
    this.updateSearchHistoryUI();
  }

  bindNavigation() {
    const navItems = document.querySelectorAll(".nav-item");
    navItems.forEach((item) => {
      item.addEventListener("click", (e) => {
        const section = e.target.dataset.section;
        this.switchSection(section);
      });
    });
  }

  switchSection(sectionName) {
    // Update navigation
    document.querySelectorAll(".nav-item").forEach((item) => {
      item.classList.remove("active");
    });
    document
      .querySelector(`[data-section="${sectionName}"]`)
      .classList.add("active");

    // Update sections
    document.querySelectorAll(".section").forEach((section) => {
      section.classList.remove("active");
    });
    document.getElementById(sectionName).classList.add("active");

    this.currentSection = sectionName;
    this.hideResults();
  }

  bindForms() {
    // Pharmacy forms
    document
      .getElementById("getPharmacyForm")
      .addEventListener("submit", (e) => {
        e.preventDefault();
        this.handleGetPharmacy();
      });

    document.getElementById("pharmacyForm").addEventListener("submit", (e) => {
      e.preventDefault();
      const action = e.submitter.dataset.action;
      this.handlePharmacySubmit(action);
    });

    // Patient forms
    document
      .getElementById("getPatientForm")
      .addEventListener("submit", (e) => {
        e.preventDefault();
        this.handleGetPatient();
      });

    document.getElementById("patientForm").addEventListener("submit", (e) => {
      e.preventDefault();
      const action = e.submitter.dataset.action;
      this.handlePatientSubmit(action);
    });

    // Advanced Patient Search form
    document
      .getElementById("advancedPatientSearchForm")
      .addEventListener("submit", (e) => {
        e.preventDefault();
        this.handleAdvancedPatientSearch();
      });

    // Add real-time validation for search inputs
    this.bindSearchValidation();
  }

  bindButtons() {
    document
      .getElementById("deletePharmacyBtn")
      .addEventListener("click", () => {
        this.handleDeletePharmacy();
      });

    document
      .getElementById("deletePatientBtn")
      .addEventListener("click", () => {
        this.handleDeletePatient();
      });

    // Advanced search buttons
    document.getElementById("clearSearchBtn").addEventListener("click", () => {
      this.clearAdvancedSearchForm();
    });

    document.getElementById("quickSearchBtn").addEventListener("click", () => {
      this.handleQuickSearch();
    });

    document
      .getElementById("hideSearchResultsBtn")
      .addEventListener("click", () => {
        this.hideSearchResults();
      });

    document
      .getElementById("exportResultsBtn")
      .addEventListener("click", () => {
        this.exportSearchResults();
      });
  }

  bindSearchValidation() {
    // Store ID validation
    document.getElementById("searchStoreId").addEventListener("input", (e) => {
      this.validateStoreId(e.target);
    });

    // Health number validation
    document
      .getElementById("searchHealthNumber")
      .addEventListener("input", (e) => {
        this.validateHealthNumber(e.target);
      });

    // Name validation
    const nameInputs = ["searchFirstName", "searchLastName"];
    nameInputs.forEach((inputId) => {
      document.getElementById(inputId).addEventListener("input", (e) => {
        this.validateNameInput(e.target);
      });
    });
  }

  validateStoreId(input) {
    const value = input.value.trim();
    if (value && !/^\d+$/.test(value)) {
      input.setCustomValidity("Store ID should contain only numbers");
      input.style.borderColor = "#ef4444";
    } else {
      input.setCustomValidity("");
      input.style.borderColor = "";
    }
  }

  validateHealthNumber(input) {
    const value = input.value.trim();
    if (value && !/^[A-Z0-9]+$/i.test(value)) {
      input.setCustomValidity(
        "Health number should contain only letters and numbers",
      );
      input.style.borderColor = "#ef4444";
    } else {
      input.setCustomValidity("");
      input.style.borderColor = "";
    }
  }

  validateNameInput(input) {
    const value = input.value.trim();
    if (value && !/^[a-zA-Z\s'-]+$/.test(value)) {
      input.setCustomValidity(
        "Name should contain only letters, spaces, apostrophes, and hyphens",
      );
      input.style.borderColor = "#ef4444";
    } else {
      input.setCustomValidity("");
      input.style.borderColor = "";
    }
  }

  // Pharmacy handlers
  async handleGetPharmacy() {
    const pharmacyId = document.getElementById("getPharmacyId").value.trim();
    if (!pharmacyId) {
      this.showAlert("Please enter a pharmacy ID", "error");
      return;
    }

    this.showLoading("Searching for pharmacy...");
    try {
      const pharmacy = await this.api.getPharmacy(pharmacyId);
      this.currentPharmacy = pharmacy;
      this.displayPharmacy(pharmacy);
      this.showAlert("Pharmacy found successfully!", "success");
    } catch (error) {
      this.showAlert(`Error: ${error.message}`, "error");
      this.hideResults();
    } finally {
      this.hideLoading();
    }
  }

  async handlePharmacySubmit(action) {
    const formData = this.getPharmacyFormData();
    if (!formData) return;

    const actionText = action === "add" ? "Adding" : "Updating";
    this.showLoading(`${actionText} pharmacy...`);

    try {
      if (action === "add") {
        await this.api.addPharmacy(formData);
        this.showAlert("Pharmacy added successfully!", "success");
      } else {
        await this.api.updatePharmacy(formData);
        this.showAlert("Pharmacy updated successfully!", "success");
      }

      // Refresh the display if we're viewing this pharmacy
      if (
        this.currentPharmacy &&
        this.currentPharmacy.pharmacyId === formData.pharmacyId
      ) {
        this.currentPharmacy = formData;
        this.displayPharmacy(formData);
      }
    } catch (error) {
      this.showAlert(`Error ${action}ing pharmacy: ${error.message}`, "error");
    } finally {
      this.hideLoading();
    }
  }

  async handleDeletePharmacy() {
    if (!this.currentPharmacy) return;

    if (
      !confirm(
        `Are you sure you want to delete pharmacy ${this.currentPharmacy.pharmacyId}?`,
      )
    ) {
      return;
    }

    this.showLoading("Deleting pharmacy...");
    try {
      await this.api.deletePharmacy(this.currentPharmacy.pharmacyId);
      this.showAlert("Pharmacy deleted successfully!", "success");
      this.hideResults();
      this.clearPharmacyForm();
    } catch (error) {
      this.showAlert(`Error deleting pharmacy: ${error.message}`, "error");
    } finally {
      this.hideLoading();
    }
  }

  // Patient handlers
  async handleGetPatient() {
    const storePatientId = document.getElementById("getPatientId").value.trim();
    if (!storePatientId) {
      this.showAlert("Please enter a store patient ID", "error");
      return;
    }

    this.showLoading("Searching for patient...");
    try {
      const patient = await this.api.getPatient(storePatientId);
      this.currentPatient = patient;
      this.displayPatient(patient);
      this.showAlert("Patient found successfully!", "success");
    } catch (error) {
      this.showAlert(`Error: ${error.message}`, "error");
      this.hideResults();
    } finally {
      this.hideLoading();
    }
  }

  async handlePatientSubmit(action) {
    const formData = this.getPatientFormData();
    if (!formData) return;

    const actionText = action === "add" ? "Adding" : "Updating";
    this.showLoading(`${actionText} patient...`);

    try {
      if (action === "add") {
        await this.api.addPatient(formData);
        this.showAlert("Patient added successfully!", "success");
      } else {
        await this.api.updatePatient(formData);
        this.showAlert("Patient updated successfully!", "success");
      }

      // Refresh the display if we're viewing this patient
      const storePatientId = `${formData.pharmacyId}-${formData.patientId}`;
      if (
        this.currentPatient &&
        this.currentPatient.pharmacyId === formData.pharmacyId &&
        this.currentPatient.patientId === formData.patientId
      ) {
        this.currentPatient = formData;
        this.displayPatient(formData);
      }
    } catch (error) {
      this.showAlert(`Error ${action}ing patient: ${error.message}`, "error");
    } finally {
      this.hideLoading();
    }
  }

  async handleDeletePatient() {
    if (!this.currentPatient) return;

    const storePatientId = `${this.currentPatient.pharmacyId}-${this.currentPatient.patientId}`;
    if (
      !confirm(
        `Are you sure you want to delete patient ${this.currentPatient.firstName} ${this.currentPatient.lastName}?`,
      )
    ) {
      return;
    }

    this.showLoading("Deleting patient...");
    try {
      await this.api.deletePatient(storePatientId);
      this.showAlert("Patient deleted successfully!", "success");
      this.hideResults();
      this.clearPatientForm();
    } catch (error) {
      this.showAlert(`Error deleting patient: ${error.message}`, "error");
    } finally {
      this.hideLoading();
    }
  }

  // Advanced Patient Search methods
  async handleAdvancedPatientSearch() {
    // Validate form before searching
    if (!this.validateSearchForm()) {
      return;
    }

    const searchCriteria = this.getSearchCriteria();

    // Check if at least one search criterion is provided
    const hasSearchCriteria = Object.values(searchCriteria).some(
      (value) => value !== null && value !== undefined && value.trim() !== "",
    );

    if (!hasSearchCriteria) {
      this.showAlert("Please enter at least one search criterion", "error");
      return;
    }

    this.showLoading("Searching patients...");

    try {
      const results = await this.api.searchPatients(searchCriteria);
      this.saveSearchHistory(searchCriteria);
      this.displaySearchResults(results, searchCriteria);
      this.showAlert(`Found ${results.length} patient(s)`, "success");
    } catch (error) {
      this.showAlert(`Search failed: ${error.message}`, "error");
    } finally {
      this.hideLoading();
    }
  }

  handleQuickSearch() {
    // Auto-fill with common search patterns
    const quickSearchOptions = [
      { label: "BC Patients", province: "BC" },
      { label: "Store 101", storeId: "101" },
      { label: "Ontario Patients", province: "ON" },
      { label: "Alberta Patients", province: "AB" },
      { label: "Quebec Patients", province: "QC" },
      { label: "Store 102", storeId: "102" },
      { label: "Store 103", storeId: "103" },
      { label: "All Smiths", lastName: "Smith" },
    ];

    // Create quick search chips if they don't exist
    let chipsContainer = document.querySelector(".quick-search-chips");
    if (!chipsContainer) {
      chipsContainer = document.createElement("div");
      chipsContainer.className = "quick-search-chips";

      quickSearchOptions.forEach((option) => {
        const chip = document.createElement("button");
        chip.className = "search-chip";
        chip.textContent = option.label;
        chip.onclick = () => {
          this.clearAdvancedSearchForm();
          if (option.storeId)
            document.getElementById("searchStoreId").value = option.storeId;
          if (option.province)
            document.getElementById("searchProvince").value = option.province;
          this.handleAdvancedPatientSearch();
        };
        chipsContainer.appendChild(chip);
      });

      document
        .getElementById("advancedPatientSearchForm")
        .appendChild(chipsContainer);
    }
  }

  clearAdvancedSearchForm() {
    document.getElementById("searchStoreId").value = "";
    document.getElementById("searchProvince").value = "";
    document.getElementById("searchFirstName").value = "";
    document.getElementById("searchLastName").value = "";
    document.getElementById("searchHealthNumber").value = "";
  }

  hideSearchResults() {
    document.getElementById("patientSearchResults").classList.add("hidden");
  }

  exportSearchResults() {
    const results = this.currentSearchResults || [];
    if (results.length === 0) {
      this.showAlert("No results to export", "info");
      return;
    }

    const csvContent = this.convertToCSV(results);
    const blob = new Blob([csvContent], { type: "text/csv;charset=utf-8;" });
    const link = document.createElement("a");
    const url = URL.createObjectURL(blob);
    link.setAttribute("href", url);
    link.setAttribute(
      "download",
      `patient_search_results_${new Date().toISOString().split("T")[0]}.csv`,
    );
    link.style.visibility = "hidden";
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    this.showAlert("Results exported successfully!", "success");
  }

  validateSearchForm() {
    const inputs = [
      document.getElementById("searchStoreId"),
      document.getElementById("searchHealthNumber"),
      document.getElementById("searchFirstName"),
      document.getElementById("searchLastName"),
    ];

    let isValid = true;
    inputs.forEach((input) => {
      if (!input.checkValidity()) {
        input.reportValidity();
        isValid = false;
      }
    });

    return isValid;
  }

  getSearchCriteria() {
    return {
      storeId:
        document.getElementById("searchStoreId").value.trim() || undefined,
      province: document.getElementById("searchProvince").value || undefined,
      firstName:
        document.getElementById("searchFirstName").value.trim() || undefined,
      lastName:
        document.getElementById("searchLastName").value.trim() || undefined,
      healthNumber:
        document.getElementById("searchHealthNumber").value.trim() || undefined,
    };
  }

  displaySearchResults(results, searchCriteria) {
    this.currentSearchResults = results;
    const container = document.getElementById("patientSearchData");
    const countElement = document.getElementById("searchResultsCount");
    const resultsSection = document.getElementById("patientSearchResults");

    countElement.textContent = `(${results.length} found)`;

    if (results.length === 0) {
      container.innerHTML = `
        <div class="search-empty-state">
          <i class="fas fa-search"></i>
          <h3>No patients found</h3>
          <p>Try adjusting your search criteria or use broader terms.</p>
        </div>
      `;
    } else {
      // Create search stats
      const stats = this.generateSearchStats(results, searchCriteria);

      let html = `
        <div class="search-stats">
          <div class="search-stat">
            <i class="fas fa-users"></i>
            <span><strong>${results.length}</strong> patients</span>
          </div>
          <div class="search-stat">
            <i class="fas fa-building"></i>
            <span><strong>${stats.uniqueStores}</strong> stores</span>
          </div>
          <div class="search-stat">
            <i class="fas fa-map-marker-alt"></i>
            <span><strong>${stats.uniqueProvinces}</strong> provinces</span>
          </div>
        </div>
        <div class="search-results-grid">
      `;

      results.forEach((patient) => {
        html += `
          <div class="search-result-item" onclick="uiManager.selectSearchResult('${patient.pharmacyId}-${patient.patientId}')">
            <div class="search-result-header">
              <h4 class="search-result-name">
                ${this.highlightSearchTerms(patient.firstName, searchCriteria.firstName)} ${this.highlightSearchTerms(patient.lastName, searchCriteria.lastName)}
                ${patient.prefName && patient.prefName !== "" ? ` (${patient.prefName})` : ""}
              </h4>
              <span class="search-result-id">${patient.pharmacyId}-${patient.patientId}</span>
            </div>
            <div class="search-result-details">
              <div class="search-detail-item">
                <span class="search-detail-label">Store ID</span>
                <span class="search-detail-value">${patient.pharmacyId}</span>
              </div>
              <div class="search-detail-item">
                <span class="search-detail-label">Province</span>
                <span class="search-detail-value">${patient.province}</span>
              </div>
              <div class="search-detail-item">
                <span class="search-detail-label">City</span>
                <span class="search-detail-value">${patient.city}</span>
              </div>
              <div class="search-detail-item">
                <span class="search-detail-label">Phone</span>
                <span class="search-detail-value">${patient.phoneNumber}</span>
              </div>
              <div class="search-detail-item">
                <span class="search-detail-label">Health Number</span>
                <span class="search-detail-value">${patient.provHealthNumber}</span>
              </div>
              <div class="search-detail-item">
                <span class="search-detail-label">Language</span>
                <span class="search-detail-value">${patient.langPref.toUpperCase()}</span>
              </div>
            </div>
          </div>
        `;
      });

      html += "</div>";
      container.innerHTML = html;
    }

    resultsSection.classList.remove("hidden");
    resultsSection.scrollIntoView({ behavior: "smooth", block: "start" });
  }

  selectSearchResult(storePatientId) {
    // Find the patient in current search results and display in the main patient view
    const patient = this.currentSearchResults.find(
      (p) => `${p.pharmacyId}-${p.patientId}` === storePatientId,
    );
    if (patient) {
      this.currentPatient = patient;
      this.displayPatient(patient);
      document.getElementById("patientResults").classList.remove("hidden");
    }
  }

  generateSearchStats(results, criteria) {
    const uniqueStores = new Set(results.map((p) => p.pharmacyId)).size;
    const uniqueProvinces = new Set(results.map((p) => p.province)).size;

    return {
      uniqueStores,
      uniqueProvinces,
    };
  }

  convertToCSV(results) {
    const headers = [
      "Store ID",
      "Patient ID",
      "First Name",
      "Last Name",
      "Preferred Name",
      "Date of Birth",
      "Phone",
      "Health Number",
      "Address",
      "City",
      "Province",
      "Postal Code",
      "Language",
      "SMS Opt-in",
    ];

    let csv = headers.join(",") + "\n";

    results.forEach((patient) => {
      const address = [
        patient.unitNumber,
        patient.streetNumber,
        patient.streetName,
      ]
        .filter(Boolean)
        .join(" ");

      const row = [
        patient.pharmacyId,
        patient.patientId,
        patient.firstName,
        patient.lastName,
        patient.prefName && patient.prefName !== "" ? patient.prefName : "",
        patient.dateOfBirth,
        patient.phoneNumber,
        patient.provHealthNumber,
        address,
        patient.city,
        patient.province,
        patient.postalCode,
        patient.langPref,
        patient.smsOptInPref ? "Yes" : "No",
      ]
        .map((field) => `"${field}"`)
        .join(",");

      csv += row + "\n";
    });

    return csv;
  }

  highlightSearchTerms(text, searchTerm) {
    if (!searchTerm || !text) return text;
    const regex = new RegExp(`(${searchTerm})`, "gi");
    return text.replace(
      regex,
      '<mark style="background-color: #fef3c7; color: #92400e; padding: 1px 3px; border-radius: 3px;">$1</mark>',
    );
  }

  // Search History Methods
  loadSearchHistory() {
    try {
      const history = localStorage.getItem("patientSearchHistory");
      return history ? JSON.parse(history) : [];
    } catch (error) {
      console.warn("Failed to load search history:", error);
      return [];
    }
  }

  saveSearchHistory(searchCriteria) {
    try {
      // Only save searches with actual criteria
      const hasValidCriteria = Object.values(searchCriteria).some(
        (value) => value && value.trim && value.trim() !== "",
      );

      if (!hasValidCriteria) return;

      // Create search entry
      const searchEntry = {
        id: Date.now(),
        criteria: { ...searchCriteria },
        timestamp: new Date().toISOString(),
        description: this.generateSearchDescription(searchCriteria),
      };

      // Add to history (keep last 10 searches)
      this.searchHistory.unshift(searchEntry);
      this.searchHistory = this.searchHistory.slice(0, 10);

      localStorage.setItem(
        "patientSearchHistory",
        JSON.stringify(this.searchHistory),
      );
      this.updateSearchHistoryUI();
    } catch (error) {
      console.warn("Failed to save search history:", error);
    }
  }

  generateSearchDescription(criteria) {
    const parts = [];
    if (criteria.storeId) parts.push(`Store: ${criteria.storeId}`);
    if (criteria.province) parts.push(`Province: ${criteria.province}`);
    if (criteria.firstName) parts.push(`First: ${criteria.firstName}`);
    if (criteria.lastName) parts.push(`Last: ${criteria.lastName}`);
    if (criteria.healthNumber) parts.push(`Health#: ${criteria.healthNumber}`);

    return parts.join(", ") || "Empty search";
  }

  updateSearchHistoryUI() {
    if (this.searchHistory.length === 0) return;

    let historyContainer = document.querySelector(".search-history-container");
    if (!historyContainer) {
      historyContainer = document.createElement("div");
      historyContainer.className = "search-history-container";
      historyContainer.innerHTML = `
        <div style="margin: 1rem 0 0.5rem 0; padding-top: 1rem; border-top: 1px solid var(--border-color);">
          <h4 style="font-size: 0.875rem; color: var(--text-secondary); margin: 0 0 0.5rem 0; display: flex; align-items: center; gap: 0.5rem;">
            <i class="fas fa-history"></i>
            Recent Searches
          </h4>
          <div class="search-history-items" style="display: flex; flex-wrap: wrap; gap: 0.5rem;"></div>
        </div>
      `;

      document
        .getElementById("advancedPatientSearchForm")
        .appendChild(historyContainer);
    }

    const itemsContainer = historyContainer.querySelector(
      ".search-history-items",
    );
    itemsContainer.innerHTML = "";

    this.searchHistory.slice(0, 5).forEach((entry) => {
      const chip = document.createElement("button");
      chip.type = "button";
      chip.className = "search-chip";
      chip.style.fontSize = "0.75rem";
      chip.style.opacity = "0.8";
      chip.textContent = entry.description;
      chip.title = `Search performed on ${new Date(entry.timestamp).toLocaleString()}`;

      chip.onclick = () => {
        this.applySearchFromHistory(entry.criteria);
      };

      itemsContainer.appendChild(chip);
    });
  }

  applySearchFromHistory(criteria) {
    // Fill form with historical search criteria
    document.getElementById("searchStoreId").value = criteria.storeId || "";
    document.getElementById("searchProvince").value = criteria.province || "";
    document.getElementById("searchFirstName").value = criteria.firstName || "";
    document.getElementById("searchLastName").value = criteria.lastName || "";
    document.getElementById("searchHealthNumber").value =
      criteria.healthNumber || "";

    // Trigger search
    this.handleAdvancedPatientSearch();
  }

  // Form data helpers
  getPharmacyFormData() {
    const pharmacyId = document.getElementById("pharmacyId").value.trim();
    const streetAddress = document
      .getElementById("pharmacyStreetAddress")
      .value.trim();
    const city = document.getElementById("pharmacyCity").value.trim();
    const province = document.getElementById("pharmacyProvince").value.trim();
    const postalCode = document
      .getElementById("pharmacyPostalCode")
      .value.trim();
    const phoneNumber = document.getElementById("pharmacyPhone").value.trim();
    const version = document.getElementById("pharmacyVersion").value.trim();

    if (
      !pharmacyId ||
      !streetAddress ||
      !city ||
      !province ||
      !postalCode ||
      !phoneNumber ||
      !version
    ) {
      this.showAlert("Please fill in all required fields", "error");
      return null;
    }

    return {
      pharmacyId,
      streetAddress,
      city,
      province,
      postalCode,
      phoneNumber,
      version,
    };
  }

  getPatientFormData() {
    const requiredFields = [
      "patientPharmacyId",
      "patientId",
      "firstName",
      "lastName",
      "dateOfBirth",
      "phoneNumber",
      "provHealthNumber",
      "streetNumber",
      "streetName",
      "city",
      "province",
      "postalCode",
      "country",
      "langPref",
    ];

    const data = {};
    for (const fieldId of requiredFields) {
      const value = document.getElementById(fieldId).value.trim();
      if (!value) {
        this.showAlert("Please fill in all required fields", "error");
        return null;
      }
      data[fieldId] = value;
    }

    // Optional fields
    const prefName = document.getElementById("prefName").value.trim();
    const unitNumber = document.getElementById("unitNumber").value.trim();

    return {
      pharmacyId: data.patientPharmacyId,
      patientId: data.patientId,
      firstName: data.firstName,
      lastName: data.lastName,
      prefName: prefName || null,
      dateOfBirth: data.dateOfBirth,
      phoneNumber: data.phoneNumber,
      provHealthNumber: data.provHealthNumber,
      unitNumber: unitNumber || null,
      streetNumber: data.streetNumber,
      streetName: data.streetName,
      city: data.city,
      province: data.province,
      postalCode: data.postalCode,
      country: data.country,
      langPref: data.langPref,
      smsOptInPref: document.getElementById("smsOptInPref").checked,
    };
  }

  // Display helpers
  displayPharmacy(pharmacy) {
    const container = document.getElementById("pharmacyData");
    container.innerHTML = `
            <div class="data-grid">
                <div class="data-item">
                    <span class="data-label">Pharmacy ID</span>
                    <span class="data-value">${pharmacy.pharmacyId}</span>
                </div>
                <div class="data-item">
                    <span class="data-label">Street Address</span>
                    <span class="data-value">${pharmacy.streetAddress}</span>
                </div>
                <div class="data-item">
                    <span class="data-label">City</span>
                    <span class="data-value">${pharmacy.city}</span>
                </div>
                <div class="data-item">
                    <span class="data-label">Province</span>
                    <span class="data-value">${pharmacy.province}</span>
                </div>
                <div class="data-item">
                    <span class="data-label">Postal Code</span>
                    <span class="data-value">${pharmacy.postalCode}</span>
                </div>
                <div class="data-item">
                    <span class="data-label">Phone Number</span>
                    <span class="data-value">${pharmacy.phoneNumber}</span>
                </div>
                <div class="data-item">
                    <span class="data-label">Version</span>
                    <span class="data-value">${pharmacy.version}</span>
                </div>
            </div>
        `;

    // Populate form for editing
    document.getElementById("pharmacyId").value = pharmacy.pharmacyId;
    document.getElementById("pharmacyStreetAddress").value =
      pharmacy.streetAddress;
    document.getElementById("pharmacyCity").value = pharmacy.city;
    document.getElementById("pharmacyProvince").value = pharmacy.province;
    document.getElementById("pharmacyPostalCode").value = pharmacy.postalCode;
    document.getElementById("pharmacyPhone").value = pharmacy.phoneNumber;
    document.getElementById("pharmacyVersion").value = pharmacy.version;

    document.getElementById("pharmacyResults").classList.remove("hidden");
  }

  displayPatient(patient) {
    const container = document.getElementById("patientData");
    container.innerHTML = `
            <div class="data-grid">
                <div class="data-item">
                    <span class="data-label">Pharmacy ID</span>
                    <span class="data-value">${patient.pharmacyId}</span>
                </div>
                <div class="data-item">
                    <span class="data-label">Patient ID</span>
                    <span class="data-value">${patient.patientId}</span>
                </div>
                <div class="data-item">
                    <span class="data-label">Full Name</span>
                    <span class="data-value">${patient.firstName} ${patient.lastName}</span>
                </div>
                <div class="data-item">
                    <span class="data-label">Preferred Name</span>
                    <span class="data-value">${patient.prefName || "N/A"}</span>
                </div>
                <div class="data-item">
                    <span class="data-label">Date of Birth</span>
                    <span class="data-value">${patient.dateOfBirth}</span>
                </div>
                <div class="data-item">
                    <span class="data-label">Phone Number</span>
                    <span class="data-value">${patient.phoneNumber}</span>
                </div>
                <div class="data-item">
                    <span class="data-label">Provincial Health Number</span>
                    <span class="data-value">${patient.provHealthNumber}</span>
                </div>
                <div class="data-item">
                    <span class="data-label">Address</span>
                    <span class="data-value">
                        ${patient.unitNumber ? patient.unitNumber + "-" : ""}${patient.streetNumber} ${patient.streetName}<br>
                        ${patient.city}, ${patient.province} ${patient.postalCode}<br>
                        ${patient.country}
                    </span>
                </div>
                <div class="data-item">
                    <span class="data-label">Language Preference</span>
                    <span class="data-value">${patient.langPref.toUpperCase()}</span>
                </div>
                <div class="data-item">
                    <span class="data-label">SMS Opt-in</span>
                    <span class="data-value">${patient.smsOptInPref ? "Yes" : "No"}</span>
                </div>
            </div>
        `;

    // Populate form for editing
    document.getElementById("patientPharmacyId").value = patient.pharmacyId;
    document.getElementById("patientId").value = patient.patientId;
    document.getElementById("firstName").value = patient.firstName;
    document.getElementById("lastName").value = patient.lastName;
    document.getElementById("prefName").value = patient.prefName || "";
    document.getElementById("dateOfBirth").value = patient.dateOfBirth;
    document.getElementById("phoneNumber").value = patient.phoneNumber;
    document.getElementById("provHealthNumber").value =
      patient.provHealthNumber;
    document.getElementById("unitNumber").value = patient.unitNumber || "";
    document.getElementById("streetNumber").value = patient.streetNumber;
    document.getElementById("streetName").value = patient.streetName;
    document.getElementById("city").value = patient.city;
    document.getElementById("province").value = patient.province;
    document.getElementById("postalCode").value = patient.postalCode;
    document.getElementById("country").value = patient.country;
    document.getElementById("langPref").value = patient.langPref;
    document.getElementById("smsOptInPref").checked = patient.smsOptInPref;

    document.getElementById("patientResults").classList.remove("hidden");
  }

  // Utility methods
  hideResults() {
    document.getElementById("pharmacyResults").classList.add("hidden");
    document.getElementById("patientResults").classList.add("hidden");
    document.getElementById("patientSearchResults").classList.add("hidden");
  }

  clearPharmacyForm() {
    document.getElementById("pharmacyForm").reset();
    document.getElementById("pharmacyVersion").value = "1.0";
  }

  clearPatientForm() {
    document.getElementById("patientForm").reset();
    document.getElementById("country").value = "Canada";
    document.getElementById("langPref").value = "en";
  }

  showAlert(message, type = "info") {
    const alertContainer = document.getElementById("alerts");
    const alertId = "alert-" + Date.now();

    const alertHTML = `
            <div id="${alertId}" class="alert alert-${type}" style="margin-bottom: 10px; animation: slideIn 0.3s ease;">
                <i class="fas fa-${this.getAlertIcon(type)}"></i>
                <span>${message}</span>
                <button onclick="document.getElementById('${alertId}').remove()" style="background: none; border: none; color: inherit; font-size: 1.2em; cursor: pointer; margin-left: auto;">
                    <i class="fas fa-times"></i>
                </button>
            </div>
        `;

    alertContainer.insertAdjacentHTML("beforeend", alertHTML);

    // Auto-remove after 5 seconds
    setTimeout(() => {
      const alertElement = document.getElementById(alertId);
      if (alertElement) {
        alertElement.style.animation = "slideOut 0.3s ease";
        setTimeout(() => alertElement.remove(), 300);
      }
    }, 5000);
  }

  getAlertIcon(type) {
    switch (type) {
      case "success":
        return "check-circle";
      case "error":
        return "exclamation-triangle";
      case "info":
        return "info-circle";
      default:
        return "info-circle";
    }
  }

  showLoading(message = "Loading...") {
    const existingLoader = document.getElementById("global-loader");
    if (existingLoader) {
      existingLoader.remove();
    }

    this.showProgressBar();

    const loaderHTML = `
            <div id="global-loader" style="
                position: fixed;
                top: 0;
                left: 0;
                right: 0;
                bottom: 0;
                background: rgba(255, 255, 255, 0.9);
                display: flex;
                align-items: center;
                justify-content: center;
                z-index: 9999;
                backdrop-filter: blur(5px);
                animation: fadeIn 0.3s ease;
            ">
                <div style="
                    background: white;
                    padding: 2rem;
                    border-radius: 1rem;
                    box-shadow: var(--shadow-xl);
                    display: flex;
                    align-items: center;
                    gap: 1rem;
                    animation: slideUp 0.3s ease;
                ">
                    <div class="loading"></div>
                    <span style="font-weight: 500; color: var(--gray-700);">${message}</span>
                </div>
            </div>
        `;

    document.body.insertAdjacentHTML("beforeend", loaderHTML);

    // Simulate progress updates
    setTimeout(() => this.updateProgress(60), 200);
    setTimeout(() => this.updateProgress(90), 600);
  }

  hideLoading() {
    const loader = document.getElementById("global-loader");
    if (loader) {
      loader.remove();
    }
    this.hideProgressBar();
  }

  // Theme Toggle Functionality
  initThemeToggle() {
    const themeToggle = document.getElementById("themeToggle");
    const prefersDark = window.matchMedia(
      "(prefers-color-scheme: dark)",
    ).matches;
    this.darkMode = localStorage.getItem("darkMode") === "true" || prefersDark;

    this.updateTheme();

    themeToggle.addEventListener("click", () => {
      this.toggleTheme();
    });
  }

  toggleTheme() {
    this.darkMode = !this.darkMode;
    localStorage.setItem("darkMode", this.darkMode);
    this.updateTheme();

    // Add a nice transition effect
    document.body.style.transition = "all 0.3s ease";
    setTimeout(() => {
      document.body.style.transition = "";
    }, 300);
  }

  updateTheme() {
    const themeIcon = document.querySelector("#themeToggle i");
    if (this.darkMode) {
      document.body.classList.add("dark-theme");
      themeIcon.className = "fas fa-sun";
    } else {
      document.body.classList.remove("dark-theme");
      themeIcon.className = "fas fa-moon";
    }
  }

  // Progress Bar
  initProgressBar() {
    this.progressBar = document.getElementById("progressBar");
    this.progressFill = this.progressBar.querySelector(".progress-fill");
  }

  showProgressBar() {
    this.progressBar.classList.remove("hidden");
    this.animateProgress(0, 30);
  }

  updateProgress(value) {
    this.progressValue = Math.min(100, Math.max(0, value));
    this.progressFill.style.width = this.progressValue + "%";
  }

  hideProgressBar() {
    this.animateProgress(this.progressValue, 100, () => {
      setTimeout(() => {
        this.progressBar.classList.add("hidden");
        this.updateProgress(0);
      }, 200);
    });
  }

  animateProgress(from, to, callback) {
    const duration = 800;
    const startTime = Date.now();

    const animate = () => {
      const elapsed = Date.now() - startTime;
      const progress = Math.min(elapsed / duration, 1);
      const value = from + (to - from) * this.easeInOutCubic(progress);

      this.updateProgress(value);

      if (progress < 1) {
        requestAnimationFrame(animate);
      } else if (callback) {
        callback();
      }
    };

    animate();
  }

  easeInOutCubic(t) {
    return t < 0.5 ? 4 * t * t * t : (t - 1) * (2 * t - 2) * (2 * t - 2) + 1;
  }

  // Scroll Effects
  initScrollEffects() {
    const scrollToTopBtn = document.getElementById("scrollToTop");

    window.addEventListener("scroll", () => {
      if (window.pageYOffset > 300) {
        scrollToTopBtn.classList.remove("hidden");
      } else {
        scrollToTopBtn.classList.add("hidden");
      }
    });

    scrollToTopBtn.addEventListener("click", () => {
      this.scrollToTop();
    });
  }

  scrollToTop() {
    window.scrollTo({
      top: 0,
      behavior: "smooth",
    });
  }

  // Interactive Effects
  addInteractiveEffects() {
    // Add ripple effect to buttons
    document.addEventListener("click", (e) => {
      if (e.target.classList.contains("btn")) {
        this.createRipple(e);
      }
    });

    // Add hover effects to cards
    document.querySelectorAll(".card").forEach((card) => {
      card.addEventListener("mouseenter", (e) => {
        this.addCardGlow(e.target);
      });

      card.addEventListener("mouseleave", (e) => {
        this.removeCardGlow(e.target);
      });
    });

    // Add shake animation for errors
    this.addShakeOnError();
  }

  createRipple(e) {
    const button = e.target;
    const ripple = document.createElement("span");
    const rect = button.getBoundingClientRect();
    const size = Math.max(rect.width, rect.height);
    const x = e.clientX - rect.left - size / 2;
    const y = e.clientY - rect.top - size / 2;

    ripple.style.cssText = `
            position: absolute;
            border-radius: 50%;
            background: rgba(255, 255, 255, 0.6);
            transform: scale(0);
            animation: ripple 0.6s linear;
            left: ${x}px;
            top: ${y}px;
            width: ${size}px;
            height: ${size}px;
            pointer-events: none;
        `;

    button.style.position = "relative";
    button.style.overflow = "hidden";
    button.appendChild(ripple);

    setTimeout(() => ripple.remove(), 600);
  }

  addCardGlow(card) {
    card.style.boxShadow = "0 0 30px rgba(37, 99, 235, 0.3)";
  }

  removeCardGlow(card) {
    card.style.boxShadow = "";
  }

  addShakeOnError() {
    const originalShowAlert = this.showAlert.bind(this);
    this.showAlert = (message, type = "info") => {
      if (type === "error") {
        // Add shake animation to the active section
        const activeSection = document.querySelector(".section.active");
        if (activeSection) {
          activeSection.style.animation = "shake 0.5s ease-in-out";
          setTimeout(() => {
            activeSection.style.animation = "";
          }, 500);
        }
      }
      originalShowAlert(message, type);
    };
  }

  // Add CSS animations dynamically
  addAnimationStyles() {
    const style = document.createElement("style");
    style.textContent = `
            @keyframes slideIn {
                from {
                    transform: translateX(100%);
                    opacity: 0;
                }
                to {
                    transform: translateX(0);
                    opacity: 1;
                }
            }

            @keyframes slideOut {
                from {
                    transform: translateX(0);
                    opacity: 1;
                }
                to {
                    transform: translateX(100%);
                    opacity: 0;
                }
            }

            @keyframes fadeIn {
                from {
                    opacity: 0;
                }
                to {
                    opacity: 1;
                }
            }

            @keyframes slideUp {
                from {
                    transform: translateY(50px);
                    opacity: 0;
                }
                to {
                    transform: translateY(0);
                    opacity: 1;
                }
            }

            @keyframes shake {
                0%, 100% { transform: translateX(0); }
                10%, 30%, 50%, 70%, 90% { transform: translateX(-5px); }
                20%, 40%, 60%, 80% { transform: translateX(5px); }
            }

            @keyframes ripple {
                to {
                    transform: scale(4);
                    opacity: 0;
                }
            }

            .alert button {
                display: flex;
                align-items: center;
                padding: 0.25rem;
            }

            .alert {
                display: flex;
                align-items: center;
                justify-content: space-between;
                gap: 1rem;
            }

            select.form-input {
                background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 20 20'%3e%3cpath stroke='%236b7280' stroke-linecap='round' stroke-linejoin='round' stroke-width='1.5' d='M6 8l4 4 4-4'/%3e%3c/svg%3e");
                background-position: right 0.5rem center;
                background-repeat: no-repeat;
                background-size: 1.5em 1.5em;
                padding-right: 2.5rem;
                appearance: none;
            }

            .dark-theme {
                filter: invert(1) hue-rotate(180deg);
            }

            .dark-theme img,
            .dark-theme video,
            .dark-theme iframe {
                filter: invert(1) hue-rotate(180deg);
            }
        `;
    document.head.appendChild(style);
  }
}

/*
 * ADVANCED PATIENT SEARCH IMPLEMENTATION SUMMARY
 * ==============================================
 *
 * This implementation adds a comprehensive patient search functionality to the Central Pharmacy Management System:
 *
 * 1. SEARCH CAPABILITIES:
 *    - Multi-field search: Store ID, Province, First Name, Last Name, Health Number
 *    - Flexible combinations: Any combination of criteria can be used
 *    - Real-time validation: Input validation for Store ID format, Health Number format, and Name format
 *    - Quick search presets: Pre-configured search buttons for common queries
 *
 * 2. USER EXPERIENCE FEATURES:
 *    - Animated search results with hover effects
 *    - Search result highlighting of matching terms
 *    - Export functionality to CSV format
 *    - Search history with localStorage persistence
 *    - Interactive result selection that populates the main patient view
 *    - Mobile-responsive design with touch-friendly interactions
 *
 * 3. API INTEGRATION:
 *    - New searchPatients() method in PharmacyAPI class
 *    - Connects to POST /patients/search endpoint
 *    - Handles all search criteria combinations defined in StorePatientRecordView
 *
 * 4. UI COMPONENTS ADDED:
 *    - Advanced search form with province dropdown and input validation
 *    - Search results grid with patient cards
 *    - Search statistics display (patient count, stores, provinces)
 *    - Quick search chips for common searches
 *    - Search history chips for recent searches
 *    - Export and close buttons for result management
 *
 * 5. PERFORMANCE & UX OPTIMIZATIONS:
 *    - CSS animations and transitions for smooth interactions
 *    - Form validation with visual feedback
 *    - Loading states and progress indicators
 *    - Error handling with user-friendly messages
 *    - Responsive design for mobile and desktop
 *
 * The implementation provides a modern, intuitive search experience that allows pharmacy staff
 * to quickly find patients across multiple stores using flexible search criteria.
 */

// Initialize the application
document.addEventListener("DOMContentLoaded", () => {
  window.uiManager = new UIManager();
  window.pharmacyUI = window.uiManager; // Keep backward compatibility
  console.log("Central Pharmacy Management System initialized");
  console.log("Advanced Patient Search functionality loaded");
});
