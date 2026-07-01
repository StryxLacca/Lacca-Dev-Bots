require('dotenv').config();

const { spawn } = require('child_process');

function startJava() {
    const java = spawn('java', [
        '-DBOT_TOKEN=' + process.env.BOT_TOKEN,
        '-jar', 'target/lacca-dev-bot-1.0.0.jar'
    ], {
        stdio: 'inherit',
        cwd: __dirname,
        env: {
            ...process.env,
            BOT_TOKEN: process.env.BOT_TOKEN
        }
    });

    java.on('exit', (code) => {
        console.log(`Java bot leállt (kód: ${code}), 10 másodperc múlva újraindul...`);
        setTimeout(startJava, 10000);
    });
}

if (!process.env.BOT_TOKEN) {
    console.error('HIBA: BOT_TOKEN hiányzik a .env fájlból!');
    process.exit(1);
}

console.log('Bot indítása...');
startJava();
