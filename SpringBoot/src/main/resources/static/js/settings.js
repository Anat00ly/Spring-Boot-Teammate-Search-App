function settingsForm() {
    return {
        formValid: false,

        init() {
            this.validateForm();
            this.setupCharCounters();
            this.setupRadioButtons();
            this.setupTelegramInput();
        },

        validateForm() {
            const requiredInputs = document.querySelectorAll('input[required]');
            this.formValid = Array.from(requiredInputs).every(input => {
                return input.value && input.value.trim() !== '';
            });
        },

        setupCharCounters() {
            document.querySelectorAll('input[maxlength]').forEach(input => {
                input.addEventListener('input', (e) => {
                    const counter = e.target.nextElementSibling?.querySelector('span');
                    if (counter) {
                        counter.textContent = e.target.value.length;
                    }
                });
            });
        },

        setupRadioButtons() {
            // Убедимся, что радиокнопки правильно отображают выбранное значение
            document.querySelectorAll('input[type="radio"]').forEach(radio => {
                if (radio.checked) {
                    console.log('Selected radio:', radio.name, radio.value);
                }
            });
        },

        setupTelegramInput() {
            const telegramInput = document.getElementById('telegram');
            if (telegramInput) {
                telegramInput.addEventListener('input', function() {
                    const value = this.value.trim();
                    if (value && !value.startsWith('@')) {
                        this.value = '@' + value.replace('@', '');
                    }
                });
            }
        }
    }
}

// Дополнительная инициализация после загрузки DOM
document.addEventListener('DOMContentLoaded', function() {
    // Логируем текущие значения для отладки
    console.log('Birthday value:', document.getElementById('birthDate')?.value);
    console.log('Country value:', document.getElementById('country')?.value);
    console.log('Timezone value:', document.getElementById('timezone')?.value);
    console.log('Games value:', document.getElementById('games')?.value);

    // Инициализация Alpine.js компонента
    if (typeof Alpine !== 'undefined') {
        Alpine.data('settingsForm', settingsForm);
    }
});