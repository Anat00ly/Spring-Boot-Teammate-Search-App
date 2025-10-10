
    function formHandler() {
    return {
    formValid: false,
    validateForm() {
    const form = this.$refs.form;
    const inputs = form.querySelectorAll('input[required], select[required]');
    this.formValid = Array.from(inputs).every(input => input.value.trim() !== '');
}
}
}
