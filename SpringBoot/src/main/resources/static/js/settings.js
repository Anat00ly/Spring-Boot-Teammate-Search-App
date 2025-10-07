function settingsForm() {
    return {
        formValid: false,

        init() {
            this.validateForm();
        },

        validateForm() {
            const requiredInputs = document.querySelectorAll('input[required]');
            this.formValid = Array.from(requiredInputs).every(input => {
                return input.value && input.value.trim() !== '';
            });
        }
    }
}

document.addEventListener('DOMContentLoaded', function() {
    const telegramInput = document.getElementById('telegram');
    if (telegramInput) {
        telegramInput.addEventListener('input', function() {
            const value = this.value.trim();
            if (value && !value.startsWith('@')) {
                this.value = '@' + value.replace('@', '');
            }
        });
    }
});