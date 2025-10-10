
    document.addEventListener('DOMContentLoaded', () => {
    const csrfMeta = document.querySelector('meta[name="_csrf"]');
    const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
    const csrfToken = csrfMeta ? csrfMeta.content : null;
    const csrfHeaderName = csrfHeaderMeta ? csrfHeaderMeta.content : 'X-CSRF-TOKEN';

    document.querySelectorAll('form.ajax-action').forEach(form => {
    form.addEventListener('submit', async (e) => {
    e.preventDefault();

    if (form.dataset.confirm === 'true') {
    if (!confirm('Удалить уведомление?')) return;
}

    try {
    const body = new FormData(form);
    const headers = {};
    if (csrfToken) headers[csrfHeaderName] = csrfToken;

    const response = await fetch(form.action, {
    method: (form.method || 'POST').toUpperCase(),
    headers: headers,
    body: body
});

    if (response.ok) {
    const card = form.closest('.notification-card');
    if (card) card.remove();
} else {
    const text = await response.text().catch(()=>null);
    alert('Ошибка: ' + (text || response.status));
}
} catch (err) {
    alert('Ошибка сети: ' + err.message);
}
});
});
});
    // markAllAsRead — минимально изменён, использует CSRF из meta
    async function markAllAsRead() {
    if (!confirm('Отметить все уведомления как прочитанные?')) return;
    const csrfMeta = document.querySelector('meta[name="_csrf"]');
    const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
    const csrfToken = csrfMeta ? csrfMeta.content : null;
    const csrfHeaderName = csrfHeaderMeta ? csrfHeaderMeta.content : 'X-CSRF-TOKEN';
    try {
    const headers = {};
    if (csrfToken) headers[csrfHeaderName] = csrfToken;
    const response = await fetch('/notifications/markAllAsRead', { // ИЗМЕНИЛ НА markAllAsRead
    method: 'POST',
    headers: headers
});
    if (response.ok) {
    // Перезагружаем страницу для обновления данных
    location.reload();
} else {
    alert('Ошибка при отметке уведомлений');
}
} catch (e) {
    alert('Ошибка сети: ' + e.message);
}
}
