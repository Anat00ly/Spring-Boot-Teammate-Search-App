
    // Простая анимация появления формы
    document.addEventListener('DOMContentLoaded', function() {
    const container = document.querySelector('.login-container');
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
        this.parentElement.style.transform = 'scale(1.02)';
    });

    input.addEventListener('blur', function() {
    this.parentElement.style.transform = 'scale(1)';
});
});

    // Анимация кнопки при клике
    document.querySelector('.form-button').addEventListener('click', function() {
    this.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Вход...';
});
