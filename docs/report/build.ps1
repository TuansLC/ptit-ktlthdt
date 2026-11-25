# Tao bao cao Bai tap 2: BaiTap2_BieuDoLop_ERD.docx va .pdf trong docs\report
#   1. (tuy chon) ve lai bieu do PlantUML: diagrams\*.puml -> img\*.png     (-Diagrams)
#   2. sinh .docx tu noi-dung.txt bang Apache POI (thu muc generator)
#   3. mo bang Microsoft Word de cap nhat muc luc, danh muc hinh/bang, so trang; luu .docx va xuat .pdf
# Chay tu thu muc goc project:  powershell -ExecutionPolicy Bypass -File docs\report\build.ps1 [-Diagrams]
param([switch]$Diagrams)
$ErrorActionPreference = 'Stop'
$report = Split-Path -Parent $MyInvocation.MyCommand.Path
$project = Split-Path -Parent (Split-Path -Parent $report)
# Word chay an tren may phat trien bi treo khi luu vao mot so thu muc; luu vao target\ roi moi chuyen ve docs\report
$work = Join-Path $project 'target'
New-Item -ItemType Directory -Force $work | Out-Null

if ($Diagrams) {
    $jar = Join-Path $work 'tools\plantuml-1.2024.7.jar'
    if (-not (Test-Path $jar)) {
        & mvn -B -q dependency:copy '-Dartifact=net.sourceforge.plantuml:plantuml:1.2024.7' "-DoutputDirectory=$work\tools"
    }
    Push-Location (Join-Path $report 'diagrams')
    try { & java -jar $jar -charset UTF-8 -tpng -o ..\img 'h*.puml' } finally { Pop-Location }
    Write-Output 'diagrams rendered'
}

$raw = Join-Path $work 'BaiTap2_raw.docx'
Push-Location (Join-Path $report 'generator')
try {
    & mvn -B -q compile exec:java "-Dexec.args=$report\noi-dung.txt $report\img $raw"
    if ($LASTEXITCODE -ne 0) { throw 'ReportGenerator failed' }
} finally { Pop-Location }

$outDocx = Join-Path $work 'BaiTap2_BieuDoLop_ERD.docx'
$outPdf = Join-Path $work 'BaiTap2_BieuDoLop_ERD.pdf'
$log = Join-Path $work 'finalize.log'
Remove-Item $outDocx, $outPdf -ErrorAction SilentlyContinue
$p = Start-Process powershell -ArgumentList '-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', (Join-Path $report 'finalize-word.ps1'),
    $raw, $outDocx, $outPdf, $log -PassThru -WindowStyle Hidden
if (-not $p.WaitForExit(300000)) {
    $p.Kill()
    throw "Word khong phan hoi sau 5 phut; xem $log"
}
Get-Content $log -Encoding UTF8 | Where-Object { $_ } | ForEach-Object { Write-Output $_ }
if (-not (Test-Path $outPdf)) { throw "Khong tao duoc PDF; xem $log" }

Move-Item $outDocx (Join-Path $report 'BaiTap2_BieuDoLop_ERD.docx') -Force
Move-Item $outPdf (Join-Path $report 'BaiTap2_BieuDoLop_ERD.pdf') -Force
Write-Output "done: $report\BaiTap2_BieuDoLop_ERD.docx / .pdf"
