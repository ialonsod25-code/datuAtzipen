# Datu-atzipena Java programa JAR exekutagarri gisa eraikitzen du (dist\ karpetan).
# Erabilera:  powershell -ExecutionPolicy Bypass -File eraiki.ps1
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot

function Bilatu($izena) {
    $cmd = Get-Command $izena -ErrorAction SilentlyContinue
    if ($cmd) { return $cmd.Source }
    if ($env:JAVA_HOME) {
        $p = Join-Path $env:JAVA_HOME "bin\$izena.exe"
        if (Test-Path $p) { return $p }
    }
    throw "$izena ez da aurkitu. Instalatu JDK bat edo definitu JAVA_HOME."
}
$javac = Bilatu 'javac'
$jar   = Bilatu 'jar'

$klaseak  = Join-Path $PSScriptRoot 'target\jar-klaseak'
$manifest = Join-Path $PSScriptRoot 'target\jar-manifest.txt'
$dist     = Join-Path $PSScriptRoot 'dist'

Remove-Item $klaseak -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item $dist -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory $klaseak | Out-Null
New-Item -ItemType Directory (Join-Path $dist 'lib') | Out-Null

# 1. Konpilatu (Java 8 mailarekin, proiektuak JavaSE-1.8 erabiltzen baitu)
$iturriak = (Get-ChildItem 'src\main\java' -Filter *.java).FullName
& $javac --release 8 -Xlint:-options -encoding UTF-8 -d $klaseak $iturriak
if ($LASTEXITCODE -ne 0) { throw 'Konpilazioak huts egin du.' }

# 2. JAR-a: Main-Class eta Oracle driver-a (lib\ojdbc11.jar) Class-Path-ean
[System.IO.File]::WriteAllText($manifest, "Main-Class: Importazioa`r`nClass-Path: lib/ojdbc11.jar`r`n")
& $jar cfm (Join-Path $dist 'kanpinak.jar') $manifest -C $klaseak .
if ($LASTEXITCODE -ne 0) { throw 'JAR-a sortzeak huts egin du.' }

# 3. Driver-a eta konfigurazioa (programak karpeta honetatik exekutatu behar du)
Copy-Item 'scripts\ojdbc11.jar' (Join-Path $dist 'lib\ojdbc11.jar')
foreach ($f in 'KONFIGURAZIOA.txt', '3PAG2_E1_kanpinak.xml', '3PAG2_E1_bezeroak.xml') {
    if (Test-Path $f) { Copy-Item $f $dist }
}

Write-Host "Eginda: $dist\kanpinak.jar"
Write-Host "Exekutatu:  cd dist ; java -jar kanpinak.jar inportatu"