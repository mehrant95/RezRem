(function($){
    $('#exit').on('click', function () {
        $('body > .centered').fadeOut(function () {
            Exit();
        });
        $('#name-container').fadeOut(function() {});
    });
})(jQuery);