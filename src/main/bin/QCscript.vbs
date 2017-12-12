 '%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
 '%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

' Test Automatique QC <-> IMSLoader

 '%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
 '%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

 ' recuperation de parametres dans Excel
Dim objExcel, xlsFile, xlsSheet

xlsFilePath = "C:\scripts_QC_Auto\"
xlsFileName = "QCparam.xls"
xlsSheetIndex = 1

' Create an excel object
Set objExcel = CreateObject("Excel.Application")
' Open the excel workbook
Set xlsFile = objExcel.Workbooks.Open (xlsFilePath & xlsFileName)
' Select the excel sheet to use
Set xlsSheet = xlsFile.Worksheets(xlsSheetIndex)

Function GetXlsValue(column, row)
Dim str_CaseXls, str_CaseRecup
str_CaseXls = column & row
str_CaseRecup = str_CaseXls & ":" & str_CaseXls
GetXlsValue = xlsSheet.Range(Str_CaseRecup).Value
End Function

 ' Parametres de configuration pour lancer IMSLoader

   'login de la machine cible IMSLoader
   login_target = GetXlsValue("B", 7)

   'passwd de la machine cible IMSLoader
   passwd_target = GetXlsValue("B", 8)

   'nom de la session PuTTY pour acceder a la cible IMSLoader
   putty_session = GetXlsValue("B", 10)

   'fichier de test IMSLoader (chemin complet)
   ' ATTENTION : pas d'espace dans le chemin d'installation
   test = GetXlsValue("B", 9)

   'chemin du startCmd (chemin complet)
   ' ATTENTION : pas d'espace dans le chemin d'installation
   path_IMSLoader = GetXlsValue("B", 12)

   'Chemin complet de la commande Plink qui permet de se connecter a IMSLoader a distance
   ' ATTENTION : pas d'espace dans le chemin d'installation
   path_Plink = GetXlsValue("B", 14)

   ' Pause a la fin du script pour avoir le temps de lire la reponse (debug)
   pause_fin_script = GetXlsValue("B", 16)


'%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
'%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

' ----------------------------------------------------
' Main Test Function
' Debug - Boolean. Equals to false if running in [Test Mode] : reporting to Quality Center
' CurrentTestSet - [OTA COM Library].TestSet.
' CurrentTSTest - [OTA COM Library].TSTest.
' CurrentRun - [OTA COM Library].Run.
' ----------------------------------------------------
Sub Test_Main(Debug, CurrentTestSet, CurrentTSTest, CurrentRun)
  ' *** VBScript Limitation ! ***
  ' "On Error Resume Next" statement suppresses run-time script errors.
  ' To handle run-time error in a right way, you need to put "If Err.Number <> 0 Then"
  ' after each line of code that can cause such a run-time error.
  'On Error Resume Next

  ' clear output window
  TDOutput.Clear

  'testcase = CurrentTestSet.Name
  testcase = CurrentTSTest.Test.Name

  'Commande en ligne a passer pour executer le testcase
  Commande = path_Plink & " -l " & login_target & " -pw " & passwd_target & " " & putty_session & " " & path_IMSLoader & " " & test & " " & testcase

  'XTools.run(Command, [Args = ""], [Timeout = -1], [UseOutput = TRUE])
  'execute la commande SSH ci-dessus en ligne
  '     Args      : arguments de l'application. Optionnel.
  '     Timeout   : delai en millisecondes. S'il est -1,
  '                 TestDirector attend jusqu'a ce que
  '                 l'application ait fini de s'excuter.
  '     UseOutput : si  TRUE, TestDirector remplace la
  '                 sortie standard par la fenetre de sortie
  ' de VAPI-XP.

  cnx_ims_result = XTools.run (Commande, "" , -1 ,TRUE)

   resultat = TDOutput.Text

   'Test si FAILING dans resultat => FAILED
   trouve = InStr(resultat,"FAILING")
   TDOutput.Print "resultat" & resultat
   If trouve <> 0 Then
    TDOutput.Print "le testCase est failed "
    TDOutput.Print "trouve = " & trouve
    CurrentRun.Status = "Failed"
    CurrentTSTest.Status = "Failed"
    sleep pause_fin_script
objExcel.Workbooks.close
Set objExcel = nothing
Set xlsFile  = nothing
Set xlsSheet = nothing
    Exit Sub
   End If

   'Test si "Run TestCaseName test" dans resultat => PASSED
   trouve = InStr(resultat,"Run " & testcase & " test")
   TDOutput.Print "resultat" & resultat
   If trouve <> 0 Then
    TDOutput.Print "le testCase est Passed "
    TDOutput.Print "trouve = " & trouve
    CurrentRun.Status = "Passed"
    CurrentTSTest.Status = "Passed"
    sleep pause_fin_script
objExcel.Workbooks.close
Set objExcel = nothing
Set xlsFile  = nothing
Set xlsSheet = nothing
    Exit Sub
   End If

   ' dans les autres cas, le resultat est N/A
   TDOutput.Print "le testCase est N/A "
   TDOutput.Print "trouve = " & trouve
   CurrentRun.Status = "N/A"
    CurrentTSTest.Status = "N/A"
    sleep pause_fin_script
objExcel.Workbooks.close
Set objExcel = nothing
Set xlsFile  = nothing
Set xlsSheet = nothing
   Exit Sub

End Sub
