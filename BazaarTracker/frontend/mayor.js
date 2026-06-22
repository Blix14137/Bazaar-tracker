async function loadCurrentMayor() {

    const mayorName =
        document.getElementById("mayorName");

    const mayorPerks =
        document.getElementById("mayorPerks");

    if (!mayorName || !mayorPerks) {
        return;
    }

    try {

        const response =
            await fetch("http://localhost:8080/api/mayor");

        if (!response.ok) {
            throw new Error("Could not load mayor");
        }

        const mayor =
            await response.json();

        mayorName.textContent =
            mayor.name;

        mayorPerks.textContent =
            mayor.perks
                .map(perk => perk.name)
                .join(" | ");

    } catch (error) {

        console.error(error);
        mayorName.textContent = "Unavailable";
        mayorPerks.textContent =
            "Could not load the current mayor.";
    }
}

loadCurrentMayor();
