(function($){

    var elements = $('body > div');

    $('#return').on('click', function () {
        $.when(elements.fadeOut()).done(function () {
            Return();
        });
    });

    $('#select-reserve-days').material_select();

})(jQuery);