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

        const response = await fetch('/api/auth/login', {
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

document.getElementById("download-debit-credit").addEventListener("click", () => {
    const { jsPDF } = window.jspdf;
    const doc = new jsPDF();

    doc.text("Тип транзакций: дебет/кредит", 10, 10);

    // Используем html2canvas для захвата графиков
    html2canvas(document.getElementById("debitChart")).then((canvas) => {
        const debitChartImage = canvas.toDataURL("image/png");
        doc.addImage(debitChartImage, "PNG", 10, 20, 180, 100); // График дебетовых транзакций
    });

    html2canvas(document.getElementById("creditChart")).then((canvas) => {
        const creditChartImage = canvas.toDataURL("image/png");
        doc.addImage(creditChartImage, "PNG", 10, 130, 180, 100); // График кредитовых транзакций
        doc.save("debit-credit.pdf");
    });
});

document.getElementById("download-income-expense").addEventListener("click", () => {
    const { jsPDF } = window.jspdf;
    const doc = new jsPDF();

    doc.text("Поступления / Расходы", 10, 10);

    // Захватываем изображения для бар и pie графиков
    html2canvas(document.getElementById("incomeExpenseBarChart")).then((canvas) => {
        const barChartImage = canvas.toDataURL("image/png");
        doc.addImage(barChartImage, "PNG", 10, 20, 180, 100); // Бар график
    });

    html2canvas(document.getElementById("incomeExpensePieChart")).then((canvas) => {
        const pieChartImage = canvas.toDataURL("image/png");
        doc.addImage(pieChartImage, "PNG", 10, 130, 180, 100); // Pie график
        doc.save("income-expense.pdf");
    });
});

document.getElementById("download-status").addEventListener("click", () => {
    const { jsPDF } = window.jspdf;
    const doc = new jsPDF();

    doc.text("Статусы транзакций", 10, 10);

    // Захватываем pie график
    html2canvas(document.getElementById("statusPieChart")).then((canvas) => {
        const pieChartImage = canvas.toDataURL("image/png");
        doc.addImage(pieChartImage, "PNG", 10, 20, 180, 100); // Pie график
        doc.save("status-transactions.pdf");
    });
});

document.getElementById("download-banks").addEventListener("click", () => {
    const { jsPDF } = window.jspdf;
    const doc = new jsPDF();

    doc.text("Банки отправители / получатели", 10, 10);

    // Захватываем изображения для графиков банков
    html2canvas(document.getElementById("senderBanksChart")).then((canvas) => {
        const senderChartImage = canvas.toDataURL("image/png");
        doc.addImage(senderChartImage, "PNG", 10, 20, 180, 100); // График банков отправителей
    });

    html2canvas(document.getElementById("receiverBanksChart")).then((canvas) => {
        const receiverChartImage = canvas.toDataURL("image/png");
        doc.addImage(receiverChartImage, "PNG", 10, 130, 180, 100); // График банков получателей
        doc.save("banks-transactions.pdf");
    });
});

document.getElementById("download-categories").addEventListener("click", () => {
    const { jsPDF } = window.jspdf;
    const doc = new jsPDF();

    doc.text("Категории транзакций", 10, 10);

    // Захватываем изображения для графиков категорий
    html2canvas(document.getElementById("expenseCategoriesChart")).then((canvas) => {
        const expenseChartImage = canvas.toDataURL("image/png");
        doc.addImage(expenseChartImage, "PNG", 10, 20, 180, 100); // График категорий расходов
    });

    html2canvas(document.getElementById("incomeCategoriesChart")).then((canvas) => {
        const incomeChartImage = canvas.toDataURL("image/png");
        doc.addImage(incomeChartImage, "PNG", 10, 130, 180, 100); // График категорий доходов
        doc.save("categories-transactions.pdf");
    });
});
