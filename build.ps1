# Compila la Rubrica per Java 8, esegue le verifiche automatiche (cartella test/) e crea Rubrica.jar.
# Uso (da PowerShell, nella cartella del progetto):  .\build.ps1
# Richiede un JDK 9 o successivo (per l'opzione --release).

$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    $classi = 'out/classes'
    $classiTest = 'out/test'
    $opzioni = @('--release', '8', '-encoding', 'UTF-8', '-Xlint:-options')

    if (Test-Path out) { Remove-Item -Recurse -Force out }

    $sorgenti = Get-ChildItem -Recurse -Filter *.java src | ForEach-Object FullName
    javac @opzioni -d $classi $sorgenti
    if ($LASTEXITCODE -ne 0) { throw 'Compilazione fallita' }

    $sorgentiTest = Get-ChildItem -Recurse -Filter *.java test | ForEach-Object FullName
    javac @opzioni -cp $classi -d $classiTest $sorgentiTest
    if ($LASTEXITCODE -ne 0) { throw 'Compilazione della verifica fallita' }

    foreach ($verifica in 'rubrica.VerificaPersistenza', 'rubrica.VerificaRegole') {
        java -cp ($classi, $classiTest -join [IO.Path]::PathSeparator) $verifica
        if ($LASTEXITCODE -ne 0) { throw "$verifica fallita" }
    }

    jar --create --file Rubrica.jar --main-class rubrica.Main -C $classi .
    if ($LASTEXITCODE -ne 0) { throw 'Creazione di Rubrica.jar fallita' }

    Write-Host "Creato $(Join-Path $PSScriptRoot 'Rubrica.jar')"
}
finally {
    Pop-Location
}
