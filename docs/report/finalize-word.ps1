# Mo file .docx do ReportGenerator sinh ra bang Word, cap nhat muc luc / danh muc hinh, bang, roi luu .docx va xuat .pdf.
param([string]$inDocx, [string]$outDocx, [string]$outPdf, [string]$logFile)
$ErrorActionPreference = 'Stop'
Set-Content $logFile '' -Encoding UTF8
function Log($m) { Add-Content $logFile ((Get-Date -Format 'HH:mm:ss') + ' ' + $m) -Encoding UTF8 }

$word = New-Object -ComObject Word.Application
$word.Visible = $false
$word.DisplayAlerts = 0
try {
    $doc = $word.Documents.Open($inDocx, $false, $false, $false)
    Log 'opened'
    for ($pass = 0; $pass -lt 2; $pass++) {
        foreach ($toc in $doc.TablesOfContents) { $toc.Update() }
        $doc.Fields.Update() | Out-Null
    }
    Log ('fields updated, pages=' + $doc.ComputeStatistics(2))
    $doc.SaveAs2($outDocx, 16)
    Log 'docx saved'
    $doc.ExportAsFixedFormat($outPdf, 17, $false, 0, 0, 1, 1, 0, $true, $true, 1)
    Log 'pdf exported'
    $doc.Close(0)
} catch {
    Log ('ERROR ' + $_.Exception.Message)
} finally {
    $word.Quit()
}
