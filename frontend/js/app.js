const API_BASE_URL = 'http://localhost:8080/api';

let currentEditingTask = null;
let categories = [];
let tasks = [];

document.addEventListener('DOMContentLoaded', function() {
    const token = localStorage.getItem('token');
    const username = localStorage.getItem('username');

    if (!token) {
        window.location.href = 'login.html';
        return;
    }

    document.getElementById('user-name').textContent = `Welcome, ${username}`;

    initializeApp();
});

async function initializeApp() {
    document.getElementById('category-modal').style.display = 'none';

    await loadCategories();
    await loadTasks();
    setupEventListeners();
}

function setupEventListeners() {
    document.getElementById('logout-btn').addEventListener('click', logout);

    document.getElementById('task-form').addEventListener('submit', handleTaskSubmit);
    document.getElementById('cancel-edit').addEventListener('click', cancelEdit);

    document.getElementById('category-filter').addEventListener('change', filterTasks);
    document.getElementById('status-filter').addEventListener('change', filterTasks);

    document.getElementById('add-category-btn').addEventListener('click', openCategoryModal);
    document.getElementById('category-form').addEventListener('submit', handleCategorySubmit);
    document.querySelector('.close').addEventListener('click', closeCategoryModal);
    document.getElementById('category-modal').addEventListener('click', function(e) {
        if (e.target === this) {
            closeCategoryModal();
        }
    });

    setupDropZones();
}

function setupDragAndDrop(taskElement) {
    taskElement.addEventListener('dragstart', handleDragStart);
    taskElement.addEventListener('dragend', handleDragEnd);
}

function setupDropZones() {
    const dropZones = document.querySelectorAll('.task-drop-zone');

    dropZones.forEach(zone => {
        zone.addEventListener('dragover', handleDragOver);
        zone.addEventListener('drop', handleDrop);
        zone.addEventListener('dragenter', handleDragEnter);
        zone.addEventListener('dragleave', handleDragLeave);
    });
}

let draggedElement = null;

function handleDragStart(e) {
    draggedElement = this;
    this.classList.add('dragging');
    e.dataTransfer.effectAllowed = 'move';
    e.dataTransfer.setData('text/html', this.outerHTML);
    e.dataTransfer.setData('text/plain', this.dataset.taskId);
}

function handleDragEnd(e) {
    this.classList.remove('dragging');
    draggedElement = null;
}

function handleDragOver(e) {
    e.preventDefault();
    e.dataTransfer.dropEffect = 'move';
}

function handleDragEnter(e) {
    e.preventDefault();
    this.classList.add('drag-over');
}

function handleDragLeave(e) {
    if (!this.contains(e.relatedTarget)) {
        this.classList.remove('drag-over');
    }
}

async function handleDrop(e) {
    e.preventDefault();
    this.classList.remove('drag-over');

    if (!draggedElement) return;

    const taskId = e.dataTransfer.getData('text/plain');
    const newStatusColumn = this.dataset.status;

    if (!taskId || !newStatusColumn) return;

    const statusMap = {
        'todo': 'TODO',
        'in-progress': 'IN_PROGRESS',
        'completed': 'COMPLETED'
    };

    const newStatus = statusMap[newStatusColumn];
    if (!newStatus) return;

    const task = tasks.find(t => t.id == taskId);
    if (!task) return;

    const currentStatus = task.status || (task.isCompleted ? 'COMPLETED' : 'TODO');
    if (currentStatus === newStatus) return;

    try {
        await updateTaskStatus(parseInt(taskId), newStatus);
        showNotification('Task moved successfully! 🎉', 'success');
    } catch (error) {
        console.error('Error moving task:', error);
        showNotification('Failed to move task', 'error');
    }
}

async function updateTaskStatus(taskId, status) {
    await apiCall(`/tasks/${taskId}/status?status=${status}`, {
        method: 'PATCH'
    });

    await loadTasks();
}

function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('email');
    window.location.href = 'login.html';
}

function getAuthHeader() {
    const token = localStorage.getItem('token');
    return {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
    };
}

async function apiCall(url, options = {}) {
    try {
        const response = await fetch(`${API_BASE_URL}${url}`, {
            ...options,
            headers: {
                ...getAuthHeader(),
                ...options.headers
            }
        });

        if (response.status === 401) {
            logout();
            return;
        }

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.message || 'API request failed');
        }

        return data;
    } catch (error) {
        console.error('API call error:', error);
        showNotification(error.message, 'error');
        throw error;
    }
}

async function loadCategories() {
    try {
        categories = await apiCall('/categories');
        updateCategorySelects();
        updateCategoryList();

        document.getElementById('category-modal').style.display = 'none';
    } catch (error) {
        console.error('Error loading categories:', error);
    }
}

function updateCategorySelects() {
    const categoryFilter = document.getElementById('category-filter');
    const taskCategory = document.getElementById('task-category');

    categoryFilter.innerHTML = '<option value="">All Categories</option>';
    taskCategory.innerHTML = '<option value="">No Category</option>';

    categories.forEach(category => {
        const filterOption = document.createElement('option');
        filterOption.value = category.id;
        filterOption.textContent = category.name;
        categoryFilter.appendChild(filterOption);

        const taskOption = document.createElement('option');
        taskOption.value = category.id;
        taskOption.textContent = category.name;
        taskCategory.appendChild(taskOption);
    });
}

function updateCategoryList() {
    const categoryList = document.getElementById('category-list');
    categoryList.innerHTML = '';

    categories.forEach(category => {
        const categoryItem = document.createElement('div');
        categoryItem.className = 'category-item';
        categoryItem.innerHTML = `
            <div class="category-info">
                <div class="category-color" style="background-color: ${category.colorCode}"></div>
                <div>
                    <div><strong>${category.name}</strong></div>
                    <div>${category.description || ''}</div>
                </div>
            </div>
            <div class="category-actions">
                <button class="btn btn-danger btn-small" onclick="deleteCategory(${category.id})">Delete</button>
            </div>
        `;
        categoryList.appendChild(categoryItem);
    });
}

async function handleCategorySubmit(e) {
    e.preventDefault();

    const name = document.getElementById('category-name').value;
    const description = document.getElementById('category-description').value;
    const colorCode = document.getElementById('category-color').value;

    try {
        await apiCall('/categories', {
            method: 'POST',
            body: JSON.stringify({
                name: name,
                description: description,
                colorCode: colorCode
            })
        });

        document.getElementById('category-form').reset();
        document.getElementById('category-color').value = '#007bff';

        await loadCategories();

        showNotification('Category created successfully!', 'success');
    } catch (error) {
        console.error('Error creating category:', error);
    }
}

async function deleteCategory(categoryId) {
    if (!confirm('Are you sure you want to delete this category? All tasks in this category will be uncategorized.')) {
        return;
    }

    try {
        await apiCall(`/categories/${categoryId}`, {
            method: 'DELETE'
        });

        await loadCategories();
        await loadTasks();

        showNotification('Category deleted successfully!', 'success');
    } catch (error) {
        console.error('Error deleting category:', error);
    }
}

async function loadTasks() {
    try {
        const categoryId = document.getElementById('category-filter').value;
        const isCompleted = document.getElementById('status-filter').value;

        let url = '/tasks?';
        if (categoryId) url += `categoryId=${categoryId}&`;
        if (isCompleted !== '') url += `isCompleted=${isCompleted}&`;

        tasks = await apiCall(url);
        updateTaskList();
    } catch (error) {
        console.error('Error loading tasks:', error);
    }
}

function updateTaskList() {
    document.getElementById('todo-tasks').innerHTML = '';
    document.getElementById('in-progress-tasks').innerHTML = '';
    document.getElementById('completed-tasks').innerHTML = '';

    if (tasks.length === 0) {
        document.getElementById('todo-tasks').innerHTML = '<p class="empty-state">🎯 Create your first task!</p>';
        return;
    }

    const todoTasks = tasks.filter(task => task.status === 'TODO' || (!task.status && !task.isCompleted));
    const inProgressTasks = tasks.filter(task => task.status === 'IN_PROGRESS');
    const completedTasks = tasks.filter(task => task.status === 'COMPLETED' || task.isCompleted);

    renderTasksInColumn(todoTasks, 'todo-tasks');
    renderTasksInColumn(inProgressTasks, 'in-progress-tasks');
    renderTasksInColumn(completedTasks, 'completed-tasks');

    if (todoTasks.length === 0) {
        document.getElementById('todo-tasks').innerHTML = '<p class="empty-state">📝 No pending tasks</p>';
    }
    if (inProgressTasks.length === 0) {
        document.getElementById('in-progress-tasks').innerHTML = '<p class="empty-state">⚡ No tasks in progress</p>';
    }
    if (completedTasks.length === 0) {
        document.getElementById('completed-tasks').innerHTML = '<p class="empty-state">✅ No completed tasks</p>';
    }
}

function renderTasksInColumn(taskList, containerId) {
    const container = document.getElementById(containerId);

    taskList.forEach(task => {
        const taskItem = createTaskElement(task);
        container.appendChild(taskItem);
    });
}

function createTaskElement(task) {
    const taskItem = document.createElement('div');
    taskItem.className = `task-item ${task.isCompleted ? 'completed' : ''}`;
    taskItem.draggable = true;
    taskItem.dataset.taskId = task.id;

    const dueDate = task.dueDate ? new Date(task.dueDate).toLocaleString() : '';
    const category = task.category ? task.category : null;

    taskItem.innerHTML = `
        <div class="task-header">
            <div class="task-title ${task.isCompleted ? 'completed' : ''}">${task.title}</div>
            <div class="task-due-date">${dueDate}</div>
        </div>
        <div class="task-meta">
            ${category ? `<span class="task-category" style="background-color: ${category.colorCode}">${category.name}</span>` : ''}
            <span class="task-priority priority-${task.priority.toLowerCase()}">${task.priority}</span>
            <span>Created: ${new Date(task.createdAt).toLocaleDateString()}</span>
        </div>
        ${task.description ? `<div class="task-description">${task.description}</div>` : ''}
        <div class="task-actions">
            <button class="btn ${task.isCompleted ? 'btn-secondary' : 'btn-success'} btn-small"
                    onclick="toggleTask(${task.id}); event.stopPropagation();">
                ${task.isCompleted ? 'Mark Pending' : 'Mark Complete'}
            </button>
            <button class="btn btn-primary btn-small" onclick="editTask(${task.id}); event.stopPropagation();">Edit</button>
            <button class="btn btn-danger btn-small" onclick="deleteTask(${task.id}); event.stopPropagation();">Delete</button>
        </div>
    `;

    setupDragAndDrop(taskItem);

    return taskItem;
}

async function handleTaskSubmit(e) {
    e.preventDefault();

    const title = document.getElementById('task-title').value;
    const description = document.getElementById('task-description').value;
    const priority = document.getElementById('task-priority').value;
    const status = document.getElementById('task-status').value;
    const categoryId = document.getElementById('task-category').value || null;
    const dueDate = document.getElementById('task-due-date').value || null;

    const taskData = {
        title: title,
        description: description,
        priority: priority,
        status: status,
        categoryId: categoryId ? parseInt(categoryId) : null,
        dueDate: dueDate ? new Date(dueDate).toISOString() : null
    };

    try {
        if (currentEditingTask) {
            await apiCall(`/tasks/${currentEditingTask}`, {
                method: 'PUT',
                body: JSON.stringify(taskData)
            });
            showNotification('Task updated successfully!', 'success');
        } else {
            await apiCall('/tasks', {
                method: 'POST',
                body: JSON.stringify(taskData)
            });
            showNotification('Task created successfully!', 'success');
        }

        document.getElementById('task-form').reset();
        cancelEdit();

        await loadTasks();
    } catch (error) {
        console.error('Error saving task:', error);
    }
}

function editTask(taskId) {
    const task = tasks.find(t => t.id === taskId);
    if (!task) return;

    document.getElementById('task-title').value = task.title;
    document.getElementById('task-description').value = task.description || '';
    document.getElementById('task-priority').value = task.priority;
    document.getElementById('task-status').value = task.status || (task.isCompleted ? 'COMPLETED' : 'TODO');
    document.getElementById('task-category').value = task.category ? task.category.id : '';

    if (task.dueDate) {
        const dueDate = new Date(task.dueDate);
        document.getElementById('task-due-date').value = dueDate.toISOString().slice(0, 16);
    }

    currentEditingTask = taskId;
    document.querySelector('#task-form button[type="submit"]').textContent = 'Update Task';
    document.getElementById('cancel-edit').style.display = 'inline-block';

    document.querySelector('.task-form-container').scrollIntoView({ behavior: 'smooth' });
}

function cancelEdit() {
    currentEditingTask = null;
    document.querySelector('#task-form button[type="submit"]').textContent = 'Add Task';
    document.getElementById('cancel-edit').style.display = 'none';
    document.getElementById('task-form').reset();
    document.getElementById('task-status').value = 'TODO';
}

async function toggleTask(taskId) {
    try {
        await apiCall(`/tasks/${taskId}/toggle`, {
            method: 'PATCH'
        });

        await loadTasks();
        showNotification('Task status updated!', 'success');
    } catch (error) {
        console.error('Error toggling task:', error);
    }
}

async function deleteTask(taskId) {
    if (!confirm('Are you sure you want to delete this task?')) {
        return;
    }

    try {
        await apiCall(`/tasks/${taskId}`, {
            method: 'DELETE'
        });

        await loadTasks();
        showNotification('Task deleted successfully!', 'success');
    } catch (error) {
        console.error('Error deleting task:', error);
    }
}

function filterTasks() {
    loadTasks();
}

function openCategoryModal() {
    document.getElementById('category-modal').style.display = 'block';
}

function closeCategoryModal() {
    document.getElementById('category-modal').style.display = 'none';
    document.getElementById('category-form').reset();
    document.getElementById('category-color').value = '#007bff';
}

function showNotification(message, type = 'info') {
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 15px 20px;
        border-radius: 4px;
        color: white;
        font-weight: 500;
        z-index: 1000;
        max-width: 300px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
    `;

    switch (type) {
        case 'success':
            notification.style.backgroundColor = '#28a745';
            break;
        case 'error':
            notification.style.backgroundColor = '#dc3545';
            break;
        case 'warning':
            notification.style.backgroundColor = '#ffc107';
            notification.style.color = '#212529';
            break;
        default:
            notification.style.backgroundColor = '#007bff';
    }

    notification.textContent = message;
    document.body.appendChild(notification);

    setTimeout(() => {
        if (notification.parentNode) {
            notification.parentNode.removeChild(notification);
        }
    }, 3000);
}