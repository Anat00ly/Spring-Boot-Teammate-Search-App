
    // Простая анимация появления формы
    document.addEventListener('DOMContentLoaded', function() {
    const container = document.querySelector('.register-container');
    container.style.opacity = '0';
    container.style.transform = 'translateY(20px)';

    setTimeout(() => {
    container.style.transition = 'opacity 0.3s ease, transform 0.3s ease';
    container.style.opacity = '1';
    container.style.transform = 'translateY(0)';
}, 100);
});

    // Обработка фокуса на полях ввода
    document.querySelectorAll('.form-input').forEach(input => {
    input.addEventListener('focus', function() {
        const wrapper = this.closest('.input-wrapper') || this.parentElement;
        wrapper.style.transform = 'scale(1.02)';

        const icon = wrapper.querySelector('.input-icon');
        if (icon) {
            icon.style.color = 'var(--accent-purple)';
        }
    });

    input.addEventListener('blur', function() {
    const wrapper = this.closest('.input-wrapper') || this.parentElement;
    wrapper.style.transform = 'scale(1)';

    const icon = wrapper.querySelector('.input-icon');
    if (icon) {
    icon.style.color = 'var(--text-muted)';
}
});
});

    // Анимация кнопки при отправке
    document.querySelector('form').addEventListener('submit', function() {
    const button = this.querySelector('.form-button');
    button.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Регистрация...';
    button.disabled = true;
});

    // Валидация email в реальном времени
    document.getElementById('email').addEventListener('input', function() {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    const isValid = emailRegex.test(this.value);
    const formGroup = this.closest('.form-group');

    if (this.value && !isValid) {
    formGroup.classList.add('has-error');
    const errorMessage = formGroup.querySelector('.error-message') || document.createElement('div');
    if (!formGroup.querySelector('.error-message')) {
    errorMessage.className = 'error-message';
    errorMessage.innerHTML = '<i class="fas fa-exclamation-circle"></i> <span>Некорректный email</span>';
    formGroup.appendChild(errorMessage);
}
} else {
    formGroup.classList.remove('has-error');
    const errorMessage = formGroup.querySelector('.error-message');
    if (errorMessage) {
    errorMessage.remove();
}
}
});
