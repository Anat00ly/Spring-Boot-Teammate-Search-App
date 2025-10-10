
    // Анимация появления формы
    document.addEventListener('DOMContentLoaded', function() {
    const container = document.querySelector('.form-container');
    container.style.opacity = '0';
    container.style.transform = 'translateY(20px)';

    setTimeout(() => {
    container.style.transition = 'opacity 0.3s ease, transform 0.3s ease';
    container.style.opacity = '1';
    container.style.transform = 'translateY(0)';
}, 100);
});

    // Счетчик символов для заголовка
    const titleInput = document.getElementById('title');
    const titleCounter = document.getElementById('title-counter');

    titleInput.addEventListener('input', function() {
    const length = this.value.length;
    titleCounter.textContent = `${length}/20`;

    if (length >= 18) {
    titleCounter.classList.add('warning');
} else {
    titleCounter.classList.remove('warning');
}
});

    // Счетчик символов для описания
    const descriptionTextarea = document.getElementById('description');
    const descriptionCounter = document.getElementById('description-counter');

    descriptionTextarea.addEventListener('input', function() {
    const length = this.value.length;
    descriptionCounter.textContent = `${length}/1000`;

    if (length >= 900) {
    descriptionCounter.classList.add('warning');
} else {
    descriptionCounter.classList.remove('warning');
}
});

    // Обработка фокуса на полях ввода
    document.querySelectorAll('.form-input, .form-textarea, .form-select').forEach(input => {
    input.addEventListener('focus', function() {
        const wrapper = this.closest('.input-wrapper') || this;
        const formGroup = this.closest('.form-group');

        formGroup.style.transform = 'scale(1.02)';

        const icon = wrapper.querySelector('.input-icon');
        if (icon) {
            icon.style.color = 'var(--accent-blue)';
        }
    });

    input.addEventListener('blur', function() {
    const wrapper = this.closest('.input-wrapper') || this;
    const formGroup = this.closest('.form-group');

    formGroup.style.transform = 'scale(1)';

    const icon = wrapper.querySelector('.input-icon');
    if (icon) {
    icon.style.color = 'var(--text-muted)';
}
});
});

    // Анимация кнопки при отправке
    document.querySelector('form').addEventListener('submit', function() {
    const button = this.querySelector('.form-button-primary');
    button.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Создание...';
    button.disabled = true;
});

    // Автоматический рост textarea
    const textarea = document.getElementById('description');
    textarea.addEventListener('input', function() {
    this.style.height = 'auto';
    this.style.height = Math.min(this.scrollHeight, 300) + 'px';
});
