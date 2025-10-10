

    document.addEventListener('DOMContentLoaded', function() {
    const gameIcon = document.querySelector('.game-icon');
    const gameText = document.querySelector('.post-game span');
    if (gameIcon && gameText) {
    const iconClass = getGameIcon(gameText.textContent);
    gameIcon.className = `${iconClass} game-icon`;
}

    const container = document.querySelector('.post-container');
    if (container) {
    container.style.opacity = '0';
    container.style.transform = 'translateY(20px)';
    setTimeout(() => {
    container.style.transition = 'opacity 0.3s ease, transform 0.3s ease';
    container.style.opacity = '1';
    container.style.transform = 'translateY(0)';
}, 100);
}

    // Отправка отклика
    document.querySelectorAll('.respond-form').forEach(form => {
    form.addEventListener('submit', function() {
    const btn = this.querySelector('.btn-respond');
    const original = btn.innerHTML;
    btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Отправка...';
    btn.disabled = true;
    setTimeout(() => {
    btn.innerHTML = original;
    btn.disabled = false;
}, 2000);
});
});
});
