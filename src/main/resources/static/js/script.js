$(function () {
    // Показать модальное окно для входа
    $('#btn-nav-logo').click(function () {
        $('#loginModal').addClass('modal_active');
    });

    // Показать модальное окно для регистрации из окна входа
    $('#register-link').click(function (e) {
        e.preventDefault();
        $('#loginModal').removeClass('modal_active');
        $('#registerModal').addClass('modal_active');
    });

    // Скрыть модальное окно при клике на кнопку закрытия
    $('.modal__close-button').click(function () {
        const modal = $(this).closest('.modal');
        modal.removeClass('modal_active');

        // Если закрывается окно регистрации, открыть окно входа
        if (modal.attr('id') === 'registerModal') {
            $('#loginModal').addClass('modal_active');
        }
    });

    // Скрыть модальное окно при клике вне его контента
    $('.modal').mouseup(function (e) {
        let modalContent = $(this).find(".modal__content");
        if (!modalContent.is(e.target) && modalContent.has(e.target).length === 0) {
            $(this).removeClass('modal_active');

            // Если закрывается окно регистрации, открыть окно входа
            if ($(this).attr('id') === 'registerModal') {
                $('#loginModal').addClass('modal_active');
            }
        }
    });
});

// Активная страница
$(document).ready(function() {
    var path = window.location.pathname.split("/").pop();

    $('.nav-link').each(function() {
        if ($(this).attr('href') === path) {
            $(this).addClass('active');
        }
    });
});

    // Авторизация
    document.querySelector('.b').addEventListener('submit', async function (e) {
        e.preventDefault();
        const username = document.getElementById('login').value;
        const password = document.getElementById('lpassword').value;

        const response = await fetch('/api/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        const result = await response.json();

        if (response.ok) {
            alert('Успешный вход: ' + result.username);
            // Закрытие модального окна
            document.getElementById('loginModal').style.display = 'none';
            // здесь можно добавить редирект или обновление страницы
        } else {
            alert('Ошибка входа: ' + result.error);
        }
    });


