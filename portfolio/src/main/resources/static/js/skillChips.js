const MIN_BRIGHTNESS_FACTOR = 0.5
const MAX_BRIGHTNESS_FACTOR = 1.5

/**
 * Creates HTMl for a skill chip with the given skill name and frequency
 *
 * @param skillName The name to be displayed in the skill chip.
 * @param frequency A float representing how much a skill has been used. It will be used to colour the skill chip
 * @returns {string} The string of HTMl representing the skill chip.
 */
function createSkillChip(skillName, frequency=10) {

    const scaledFrequency = frequency * (MAX_BRIGHTNESS_FACTOR - MIN_BRIGHTNESS_FACTOR) + MIN_BRIGHTNESS_FACTOR

    return `
        <div class="chip skillChip" style="filter: brightness(${scaledFrequency})">
            <p class="chipText">${sanitise(skillName)}</p>
        </div>`
}