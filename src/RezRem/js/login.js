(function($){
    $('#login').on('click', function () {
        Login($('#student_number').val(), $('#password').val());
    });
})(jQuery);