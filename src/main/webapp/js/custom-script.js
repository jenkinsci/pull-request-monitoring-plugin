function allowDrop(ev) {
    ev.target.classList.add('shaker');
    ev.preventDefault();
}

function cancelShake(ev) {
    ev.target.classList.remove('shaker');
}

function drag(ev) {
    ev.dataTransfer.setData("item", ev.target.id);
    ev.target.classList.remove('shaker');
}

function drop(ev) {
    ev.preventDefault();
    var data = ev.dataTransfer.getData("item");
    ev.target.appendChild(document.getElementById(data));
    ev.target.classList.remove('shaker');
}

function editLayout() {
    const save = document.getElementById('save')
    const edit = document.getElementById('edit')

    save.classList.remove('disabled');
    edit.classList.add('disabled');

    const divs = document.querySelectorAll('[id^="div"]');
    Array.prototype.forEach.call(divs, addDottedClass);

    const plugins = document.querySelectorAll('[id^="plugin"]');
    Array.prototype.forEach.call(plugins, addDraggableAttribute);
}

function saveLayout() {
    const save = document.getElementById('save')
    const edit = document.getElementById('edit')

    edit.classList.remove('disabled');
    save.classList.add('disabled');

    const divs = document.querySelectorAll('[id^="div"]');
    Array.prototype.forEach.call(divs, removeDottedClass);

    const plugins = document.querySelectorAll('[id^="plugin"]');
    Array.prototype.forEach.call(plugins, removeDraggableAttribute);
}

function addDottedClass(element) {
    element.classList.add('dotted');
}

function removeDottedClass(element) {
    element.classList.remove('dotted');
}

function addDraggableAttribute(element) {
    element.setAttribute("draggable", "true");
}

function removeDraggableAttribute(element) {
    element.setAttribute("draggable", "false");
}