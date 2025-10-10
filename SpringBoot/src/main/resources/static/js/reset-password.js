
    // Анимация появления формы
    document.addEventListener('DOMContentLoaded', function() {
    const container = document.querySelector('.reset-password-container');
    container.style.opacity = '0';
    container.style.transform = 'translateY(20px)';

    setTimeout(() => {
    container.style.transition = 'opacity 0.3s ease, transform 0.3s ease';
    container.style.opacity = '1';
    container.style.transform = 'translateY(0)';
}, 100);
});

    // Проверка силы пароля
    const passwordInput = document.getElementById('password');
    const confirmPasswordInput = document.getElementById('confirmPassword');
    const strengthIndicator = document.getElementById('passwordStrength');
    const strengthFill = document.getElementById('strengthFill');
    const strengthText = document.getElementById('strengthText');
    const submitBtn = document.getElementById('submitBtn');

    const requirements = {
    length: document.getElementById('lengthReq'),
    upper: document.getElementById('upperReq'),
    lower: document.getElementById('lowerReq'),
    number: document.getElementById('numberReq'),
    special: document.getElementById('specialReq')
};

    function updateRequirement(element, met) {
    const icon = element.querySelector('i');
    if (met) {
    element.classList.remove('unmet');
    element.classList.add('met');
    icon.className = 'fas fa-check';
} else {
    element.classList.remove('met');
    element.classList.add('unmet');
    icon.className = 'fas fa-times';
}
}

    function checkPasswordStrength(password) {
    const checks = {
    length: password.length >= 8,
    upper: /[A-Z]/.test(password),
    lower: /[a-z]/.test(password),
    number: /\d/.test(password),
    special: /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password)
};

    // Обновление требований
    updateRequirement(requirements.length, checks.length);
    updateRequirement(requirements.upper, checks.upper);
    updateRequirement(requirements.lower, checks.lower);
    updateRequirement(requirements.number, checks.number);
    updateRequirement(requirements.special, checks.special);

    // Подсчет выполненных требований
    const score = Object.values(checks).reduce((sum, check) => sum + (check ? 1 : 0), 0);

    // Обновление индикатора силы
    let strength = 'weak';
    let text = 'Слабый пароль';

    if (score >= 5) {
    strength = 'strong';
    text = 'Сильный пароль';
} else if (score >= 4) {
    strength = 'good';
    text = 'Хороший пароль';
} else if (score >= 3) {
    strength = 'fair';
    text = 'Средний пароль';
}

    strengthFill.className = `strength-fill ${strength}`;
    strengthText.textContent = text;

    return score >= 4; // Минимум 4 требования для валидного пароля
}

    passwordInput.addEventListener('input', function() {
    const password = this.value;

    if (password.length > 0) {
    strengthIndicator.style.display = 'block';
    checkPasswordStrength(password);
} else {
    strengthIndicator.style.display = 'none';
}

    validateForm();
});

    confirmPasswordInput.addEventListener('input', validateForm);

    function validateForm() {
    const password = passwordInput.value;
    const confirmPassword = confirmPasswordInput.value;
    const isPasswordStrong = password.length > 0 && checkPasswordStrength(password);
    const passwordsMatch = password === confirmPassword && password.length > 0;

    // Стиль для поля подтверждения пароля
    if (confirmPassword.length > 0) {
    if (passwordsMatch) {
    confirmPasswordInput.style.borderColor = 'var(--accent-green)';
} else {
    confirmPasswordInput.style.borderColor = 'var(--accent-red)';
}
} else {
    confirmPasswordInput.style.borderColor = 'var(--border-color)';
}

    // Активация кнопки
    submitBtn.disabled = !(isPasswordStrong && passwordsMatch);
}

    // Обработка фокуса на полях ввода
    document.querySelectorAll('.form-input').forEach(input => {
    input.addEventListener('focus', function() {
        this.parentElement.style.transform = 'scale(1.02)';
    });

    input.addEventListener('blur', function() {
    this.parentElement.style.transform = 'scale(1)';
});
});

    // Анимация кнопки при отправке
    document.getElementById('resetForm').addEventListener('submit', function(e) {
    const password = passwordInput.value;
    const confirmPassword = confirmPasswordInput.value;

    if (password !== confirmPassword) {
    e.preventDefault();
    alert('Пароли не совпадают!');
    return;
}

    if (!checkPasswordStrength(password)) {
    e.preventDefault();
    alert('Пароль не соответствует требованиям безопасности!');
    return;
}

    submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Сохранение...';
    submitBtn.disabled = true;
});
