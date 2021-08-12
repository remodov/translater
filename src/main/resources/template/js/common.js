function startSearch(searchText) {
    document.getElementsByClassName('ya-site-form__input-text')[0].value = searchText;
    document.getElementsByClassName('ya-site-form__submit')[0].click();
    return true;
}