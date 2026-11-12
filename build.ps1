# Compila la Rubrica per Java 8, esegue le verifiche automatiche (cartella test/) e crea in dist/:
#   Rubrica.jar                      eseguibile con doppio clic, con il driver MySQL incluso
#   schema_database.sql              script che crea il database
#   credenziali_database.properties  copiato dal modello solo se manca: qui vanno le credenziali vere
#   Rubrica.zip                      il pacchetto da consegnare (con il modello delle credenziali)
#
# Uso (da PowerShell, nella cartella del progetto):
#   .\build.ps1
#   .\build.ps1 -VerificaDatabase dist\credenziali_database.properties
# Richiede un JDK 9 o successivo (per l'opzione --release).

param(
    # Credenziali di un server MySQL su cui verificare anche il codice del database:
    # la verifica crea un database di prova "rubrica_verifica" e alla fine lo elimina.
    [string]$VerificaDatabase
)

$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    $driver = Get-ChildItem lib -Filter 'mysql-connector-j-*.jar' | Select-Object -First 1 -ExpandProperty FullName
    if (-not $driver) { throw 'Driver MySQL non trovato: serve lib/mysql-connector-j-<versione>.jar' }
    $separatore = [IO.Path]::PathSeparator
    $classi = 'out/classes'
    $classiTest = 'out/test'
    $contenutoJar = 'out/jar'
    $opzioni = @('--release', '8', '-encoding', 'UTF-8', '-Xlint:-options')

    if (Test-Path out) { Remove-Item -Recurse -Force out }

    $sorgenti = Get-ChildItem -Recurse -Filter *.java src | ForEach-Object FullName
    javac @opzioni -cp $driver -d $classi $sorgenti
    if ($LASTEXITCODE -ne 0) { throw 'Compilazione fallita' }

    $sorgentiTest = Get-ChildItem -Recurse -Filter *.java test | ForEach-Object FullName
    javac @opzioni -cp "$classi$separatore$driver" -d $classiTest $sorgentiTest
    if ($LASTEXITCODE -ne 0) { throw 'Compilazione delle verifiche fallita' }

    $classpath = ($classi, $classiTest, $driver) -join $separatore
    foreach ($verifica in 'rubrica.VerificaRegole', 'rubrica.VerificaHashPassword') {
        java -cp $classpath $verifica
        if ($LASTEXITCODE -ne 0) { throw "$verifica fallita" }
    }
    if ($VerificaDatabase) {
        java -cp $classpath rubrica.VerificaDatabase (Resolve-Path $VerificaDatabase) database/schema_database.sql
        if ($LASTEXITCODE -ne 0) { throw 'rubrica.VerificaDatabase fallita' }
    }

    # Rubrica.jar: classi del programma + driver MySQL estratto, così basta il doppio clic.
    # Del driver non servono il manifest (il jar ne crea uno nuovo) e INDEX.LIST, che
    # elencando solo i package del driver impedirebbe a Java 8 di trovare le classi della rubrica.
    New-Item -ItemType Directory $contenutoJar | Out-Null
    Push-Location $contenutoJar
    try {
        jar xf $driver
        if ($LASTEXITCODE -ne 0) { throw 'Estrazione del driver MySQL fallita' }
    }
    finally {
        Pop-Location
    }
    Remove-Item "$contenutoJar/META-INF/MANIFEST.MF", "$contenutoJar/META-INF/INDEX.LIST" -ErrorAction SilentlyContinue
    Copy-Item -Recurse "$classi/*" $contenutoJar

    New-Item -ItemType Directory -Force dist | Out-Null
    jar --create --file dist/Rubrica.jar --main-class rubrica.Main -C $contenutoJar .
    if ($LASTEXITCODE -ne 0) { throw 'Creazione di Rubrica.jar fallita' }

    Copy-Item database/schema_database.sql dist/
    $nuoveCredenziali = -not (Test-Path dist/credenziali_database.properties)
    if ($nuoveCredenziali) { Copy-Item database/credenziali_database.properties dist/ }
    # Nello zip va sempre il modello delle credenziali, mai quelle vere di dist/.
    Compress-Archive -Force -DestinationPath dist/Rubrica.zip `
        -Path dist/Rubrica.jar, dist/schema_database.sql, database/credenziali_database.properties

    Write-Host "Creati in $(Join-Path $PSScriptRoot 'dist'): Rubrica.jar, schema_database.sql, Rubrica.zip"
    if ($nuoveCredenziali) {
        Write-Host 'Inserire le credenziali del proprio MySQL in dist/credenziali_database.properties'
    }
}
finally {
    Pop-Location
}
