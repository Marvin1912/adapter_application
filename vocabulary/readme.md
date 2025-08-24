
# Step-by-Step Guide to Build Vocabulary App (Java + Angular)

### 1. **Project Setup**
- [x] Create a new Java Spring Boot project (backend).
- [x] Create a new Angular project (frontend).
- [x] Set up PostgreSQL locally and configure database access in Spring Boot.
- [x] Enable CORS in Spring Boot for local Angular access.

### 2. **Backend: Free Dictionary API Integration**
- [x] Create a REST controller with an endpoint `/api/define?word=...`.
- [x] Use `RestTemplate` or `WebClient` to fetch data from the Free Dictionary API.
- [x] Parse the JSON response and structure it in a DTO class.
- [x] Return the structured data to the frontend.

### 3. **Frontend: Fetch and Display Word Info**
- [x] Create a service to call the backend API with a word.
- [x] Create components to display the definition and example sentences.
- [x] Allow the user to select their preferred definition/description.
- [x] Reintegrate the edit-word component back into add-word.
- [x] Add a state for the chosen part of speech part for the validator (next point)
- [x] Implement a front validator to force the user to add to for verbs or an article for nouns.
- [x] Implement handling of multiple filters.
- [ ] Implement a check for duplicated words.
- [x] Find a way to reset the route in the browser field if a new word has been searched after selecting one from the list.
- [ ] Add error handling
- [ ] Add Deepl

### 4. **Save Final Word**
- [x] Send the selected word + description back to the backend via POST.
- [x] In the backend, create an entity for the vocabulary word and save it to PostgreSQL.

### 5. **Generate and Return CSV File**
- [x] Create a new endpoint to generate a CSV from all saved words.
- [x] Use `Jackson CSV` or similar to export word list in Anki format (e.g., front:back).
- [x] Allow download of the CSV from frontend via a button.

### 6. **Polish & Test**
- [ ] Validate inputs both in frontend and backend.
- [ ] Add loading indicators and error messages in frontend.
- [ ] Test database storage and retrieval thoroughly.
- [ ] Optionally: Add delete/edit functionality for saved words.
