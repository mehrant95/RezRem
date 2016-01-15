(function($){
    $('#login').on('click', function () {

        var result = Login($('#student_number').val(), $('#password').val());

        console.log(result);



    });
})(jQuery);