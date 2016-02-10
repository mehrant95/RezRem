(function($){
    $('#login').on('click', function () {
        Login($('#student_number').val(), $('#password').val());
    });
    $('body').keydown(function (event) {
        if (event.which == 13)
            $('#login').click();
    });
})(jQuery);