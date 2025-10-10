
    // Плавная анимация появления постов
    document.addEventListener('DOMContentLoaded', function() {
    const posts = document.querySelectorAll('.post-item');
    posts.forEach((post, index) => {
    post.style.opacity = '0';
    post.style.transform = 'translateY(20px)';

    setTimeout(() => {
    post.style.transition = 'opacity 0.3s ease, transform 0.3s ease';
    post.style.opacity = '1';
    post.style.transform = 'translateY(0)';
}, index * 100);
});

    // Обработка кнопок отклика
    document.querySelectorAll('.respond-form').forEach(form => {
    form.addEventListener('submit', function(e) {
    const btn = this.querySelector('.btn-respond');
    const originalText = btn.innerHTML;

    btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Отправка...';
    btn.disabled = true;

    setTimeout(() => {
    btn.innerHTML = originalText;
    btn.disabled = false;
}, 2000);
});
});
});
