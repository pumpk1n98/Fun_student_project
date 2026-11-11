const API_BASE = "/api/v1";
const elements = {
  account: document.querySelector("#account"),
  email: document.querySelector("#user-email"),
  loginView: document.querySelector("#login-view"),
  studentsView: document.querySelector("#students-view"),
  loginForm: document.querySelector("#login-form"),
  loginError: document.querySelector("#login-error"),
  logout: document.querySelector("#logout-button"),
  addButton: document.querySelector("#add-button"),
  search: document.querySelector("#search"),
  count: document.querySelector("#student-count"),
  rows: document.querySelector("#student-rows"),
  empty: document.querySelector("#empty-state"),
  listMessage: document.querySelector("#list-message"),
  actionsHeading: document.querySelector("#actions-heading"),
  dialog: document.querySelector("#student-dialog"),
  studentForm: document.querySelector("#student-form"),
  formError: document.querySelector("#form-error")
};

let students = [];
let currentUser = null;

function showMessage(target, text) {
  target.textContent = text;
  target.hidden = !text;
}

async function request(path, options = {}) {
  const token = sessionStorage.getItem("student-token");
  const headers = new Headers(options.headers || {});
  if (options.body) headers.set("Content-Type", "application/json");
  if (token) headers.set("Authorization", `Bearer ${token}`);

  const response = await fetch(`${API_BASE}${path}`, { ...options, headers });
  if (!response.ok) {
    if (response.status === 401) logout();
    const body = await response.json().catch(() => null);
    throw new Error(body?.message || `Не удалось выполнить запрос (${response.status}).`);
  }
  return response.status === 204 ? null : response.json();
}

function isAdmin() {
  return currentUser?.roles?.includes("ROLE_ADMIN") ?? false;
}

function showDashboard() {
  elements.loginView.hidden = true;
  elements.studentsView.hidden = false;
  elements.account.hidden = false;
  elements.email.textContent = currentUser.email;
  elements.addButton.hidden = !isAdmin();
  elements.actionsHeading.hidden = !isAdmin();
  loadStudents();
}

async function loadStudents() {
  showMessage(elements.listMessage, "");
  try {
    const result = await request("/student");
    students = result.response || [];
    elements.count.textContent = result.totalCount ?? students.length;
    renderStudents();
  } catch (error) {
    showMessage(elements.listMessage, error.message);
  }
}

function renderStudents() {
  const query = elements.search.value.trim().toLocaleLowerCase("ru");
  const visibleStudents = students.filter(student =>
    `${student.name} ${student.surname}`.toLocaleLowerCase("ru").includes(query)
  );
  elements.rows.replaceChildren();
  elements.empty.hidden = visibleStudents.length !== 0;

  visibleStudents.forEach(student => {
    const row = document.createElement("tr");
    const nameCell = document.createElement("td");
    const name = document.createElement("div");
    name.className = "student-name";
    const avatar = document.createElement("span");
    avatar.className = "avatar";
    avatar.textContent = (student.name?.[0] || "?").toLocaleUpperCase("ru");
    const fullName = document.createElement("span");
    fullName.textContent = `${student.name} ${student.surname}`;
    name.append(avatar, fullName);
    nameCell.append(name);

    const emailCell = document.createElement("td");
    emailCell.textContent = student.email || "—";
    const phoneCell = document.createElement("td");
    phoneCell.textContent = student.phoneNumber || student.phone_number || "—";
    row.append(nameCell, emailCell, phoneCell);

    if (isAdmin()) {
      const actionsCell = document.createElement("td");
      const actions = document.createElement("div");
      actions.className = "row-actions";
      const edit = document.createElement("button");
      edit.className = "row-action";
      edit.type = "button";
      edit.textContent = "Изменить";
      edit.addEventListener("click", () => editStudent(student));
      const remove = document.createElement("button");
      remove.className = "row-action delete";
      remove.type = "button";
      remove.textContent = "Удалить";
      remove.addEventListener("click", () => deleteStudent(student));
      actions.append(edit, remove);
      actionsCell.append(actions);
      row.append(actionsCell);
    }
    elements.rows.append(row);
  });
}

function openDialog(student = null) {
  elements.studentForm.reset();
  elements.formError.hidden = true;
  document.querySelector("#student-id").value = student?.id || "";
  document.querySelector("#student-name").value = student?.name || "";
  document.querySelector("#student-surname").value = student?.surname || "";
  document.querySelector("#student-email").value = student?.email || "";
  document.querySelector("#student-phone").value = student?.phoneNumber || student?.phone_number || "";
  document.querySelector("#dialog-title").textContent = student ? "Изменить контакты" : "Новый студент";
  document.querySelector("#student-name").disabled = Boolean(student);
  document.querySelector("#student-surname").disabled = Boolean(student);
  elements.dialog.showModal();
}

function editStudent(student) {
  openDialog(student);
}

async function deleteStudent(student) {
  if (!window.confirm(`Удалить студента ${student.name} ${student.surname}?`)) return;
  try {
    await request(`/student/${student.id}`, { method: "DELETE" });
    await loadStudents();
  } catch (error) {
    showMessage(elements.listMessage, error.message);
  }
}

function logout() {
  sessionStorage.removeItem("student-token");
  sessionStorage.removeItem("student-email");
  sessionStorage.removeItem("student-roles");
  currentUser = null;
  students = [];
  elements.studentsView.hidden = true;
  elements.account.hidden = true;
  elements.loginView.hidden = false;
}

elements.loginForm.addEventListener("submit", async event => {
  event.preventDefault();
  showMessage(elements.loginError, "");
  const form = new FormData(elements.loginForm);
  try {
    const auth = await request("/auth/login", {
      method: "POST",
      body: JSON.stringify({ email: form.get("email"), password: form.get("password") })
    });
    sessionStorage.setItem("student-token", auth.token);
    sessionStorage.setItem("student-email", form.get("email"));
    sessionStorage.setItem("student-roles", JSON.stringify(auth.roles));
    currentUser = { email: form.get("email"), roles: auth.roles };
    showDashboard();
  } catch (error) {
    showMessage(elements.loginError, error.message);
  }
});

elements.logout.addEventListener("click", logout);
elements.addButton.addEventListener("click", () => openDialog());
elements.search.addEventListener("input", renderStudents);
document.querySelector("#close-dialog").addEventListener("click", () => elements.dialog.close());
document.querySelector("#cancel-dialog").addEventListener("click", () => elements.dialog.close());

elements.studentForm.addEventListener("submit", async event => {
  event.preventDefault();
  const id = document.querySelector("#student-id").value;
  const data = new FormData(elements.studentForm);
  const email = data.get("email") || null;
  const phoneNumber = data.get("phoneNumber") || null;
  const options = id
    ? { method: "PUT", body: JSON.stringify({ email, phoneNumber }) }
    : {
        method: "POST",
        body: JSON.stringify({
          name: data.get("name"),
          surname: data.get("surname"),
          email,
          phone_number: phoneNumber
        })
      };
  const path = id ? `/student/${id}` : "/student/save";

  try {
    await request(path, options);
    elements.dialog.close();
    await loadStudents();
  } catch (error) {
    showMessage(elements.formError, error.message);
  }
});

async function restoreSession() {
  if (!sessionStorage.getItem("student-token")) return;
  const email = sessionStorage.getItem("student-email");
  const roles = JSON.parse(sessionStorage.getItem("student-roles") || "[]");
  if (!email) return logout();
  currentUser = { email, roles };
  showDashboard();
}

restoreSession();
