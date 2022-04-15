$(document).ready(function () {

    $('#content-form').submit(function (e) {
        e.preventDefault();
        $("#search-button").click();
        return;
    });

    $("#countOfDays").on('change', function () {
        if (this.value == null) {
            $('#table').attr('hidden', true);
        }

        $.ajax({
            type: 'GET',
            url: '/',
            data: {
                'countOfDays': this.value
            },
            beforeSend: function () {
                $('#loader').show();
            },
            success: function (data) {
                rerender('#country-form-group', data);
                $('#loader').hide();
            },
            error: function (err) {
                $('#loader').hide();
                handleError(err)
            }
        })
    })

    $("#search-button").on('click', function () {
        if (!$('#content-form')[0].checkValidity()) {
            $('#table').attr('hidden', true);
            return;
        }
        let el = document.getElementsByClassName('table-div')[0];
        el.removeAttribute('hidden');
        $.ajax({
            type: 'GET',
            url: '/search',
            data: {
                countOfDays: $('#countOfDays').val(),
                country: $('#country').val()
            },
            success: function (data) {
                rerender('#table', data);
            },
            error: function (err) {
                handleError(err)
            }
        })
    })

    $(document).on('click', ".page-link", function () {
        $.ajax({
            type: 'GET',
            url: '/search',
            data: {
                countOfDays: $('#countOfDays').val(),
                country: $('#country').val(),
                page: this.dataset.index
            },
            success: function (data) {
                rerender('#table', data);
            },
            error: function (err) {
                handleError(err)
            }
        })
    })

    $(".modal-close-js").on('click', function () {
        $('#modal').css('display', '')
    })

})

function rerender(selector, data) {
    const doc = document.implementation.createHTMLDocument();
    doc.documentElement.innerHTML = data;
    let current = document.querySelectorAll(selector);
    current[0].parentElement.replaceChild($(doc).find(selector)[0], current[0]);
}

function handleError(err) {
    $('#modal-body').text(err.getResponseHeader('error-message'));
    $('#modal').css('display', 'block')
}