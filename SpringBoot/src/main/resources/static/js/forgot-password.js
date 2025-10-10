
    // Простая анимация появления формы
    document.addEventListener('DOMContentLoaded', function() {
    const container = document.querySelector('.forgot-password-container');
    container.style.opacity = '0';
    container.style.transform = 'translateY(20px)';

    setTimeout(() => {
    container.style.transition = 'opacity 0.3s ease, transform 0.3s ease';
    container.style.opacity = '1';
    container.style.transform = 'translateY(0)';
}, 100);
});

    // Обработка фокуса на поле ввода
    document.querySelector('.form-input').addEventListener('focus', function() {
    this.parentElement.style.transform = 'scale(1.02)';
});

    document.querySelector('.form-input').addEventListener('blur', function() {
    this.parentElement.style.transform = 'scale(1)';
});

    document.addEventListener('DOMContentLoaded', function() {
    const form = document.querySelector('form');
    const button = document.querySelector('.form-button');
    form.addEventListener('submit', function() {
    button.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Отправка...';
    button.disabled = true;
});
});

