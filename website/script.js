// Set pro uchování již zkontrolovaných serverů
const checkedNodes = new Set();

// Mapa lokací a vlajek
const locationMap = {
    'in.node.moondev.eu': 'https://check-host.net/images/flags/in.png India, Mumbai',
    'jp.node.moondev.eu': 'https://check-host.net/images/flags/jp.png Japan, Osaka',
    'se.node.moondev.eu': 'https://check-host.net/images/flags/se.png Sweden, Stockholm',
    'tx.node.moondev.eu': 'https://check-host.net/images/flags/us.png USA, Dallas',
    'de.node.moondev.eu': 'https://check-host.net/images/flags/de.png Germany, Frankfurt'
};

// Funkce pro získání informací o lokalitě serveru a vlajky
function getLocationInfo(location) {
    const matchingKey = Object.keys(locationMap).find(key => location.includes(key));
    return matchingKey ? locationMap[matchingKey] : location;
}

// Funkce pro smazání tabulky
function clearTable() {
    document.getElementById('resultTableBody').innerHTML = '';
}

// Funkce pro zobrazení zprávy o načítání
function showLoadingMessage() {
    const checkingMessage = document.getElementById('checkingMessage');
    const loader = document.querySelector('.loader');

    checkingMessage.innerText = 'Checking...';
    loader.style.display = 'inline-block';
}

// Funkce pro skrytí zprávy o načítání
function hideLoadingMessage() {
    const checkingMessage = document.getElementById('checkingMessage');
    const loader = document.querySelector('.loader');

    checkingMessage.innerText = '';
    loader.style.display = 'none';
}

// Hlavní funkce pro zjištění statusu zadaného cíle na základě typu (HTTP, TCP, PING)
function checkStatus(type) {
    clearTable();
    checkedNodes.clear();
    showLoadingMessage();

    // Získání hodnoty zadané uživatelem
    const host = document.getElementById('hostInput').value;
    const url = `https://cc.moondev.eu/check-${type}?host=${host}`;

    // Získání dat pomocí API
    fetch(url)
        .then(response => response.json())
        .then(data => {
            const intervalId = setInterval(() => {
                // Aktualizování výsledku každou sekundu
                fetch(data.url)
                    .then(response => response.json())
                    .then(result => {
                        updateTable(result);
                        // Kontrola, zda-li byla získáná odpověď od všech serverů (aktuálně 5)
                        if (Object.keys(result.nodes).length === 5) {
                            clearInterval(intervalId);
                            hideLoadingMessage();
                        }
                    })
                    .catch(error => console.error('Error fetching result:', error));
            }, 1000);
        })
        .catch(error => console.error('Error fetching result URL:', error));
}

// Funkce pro aktualizaci tabulky s novými výsledky
function updateTable(result) {
    const tableBody = document.getElementById('resultTableBody');
    const locations = Object.keys(result.nodes);

    // Loopování skrz výsledky a aktualizace tabulky, také podmínka, zda-li je už server v tabulce
    locations.forEach(location => {
        if (!checkedNodes.has(location)) {
            const rowData = result.nodes[location];
            const locationInfo = getLocationInfo(location);
            const locationName = locationInfo.substring(locationInfo.indexOf(' ') + 1);
            const flagUrl = locationInfo.split(' ')[0];
            // Přidání řádku s informacemi
            const row = `<tr>
                        <td>
                            <img src="${flagUrl}" alt="${locationName}"> ${locationName}
                        </td>
                        <td>${rowData.response * 1000} ms</td>
                        <td>${rowData.code || ''}</td>
                    </tr>`;
            tableBody.innerHTML += row;
            // Přidání názvu serveru do setu
            checkedNodes.add(location);
        }
    });
}
