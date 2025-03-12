; -------------------------------------------------
; Public : Creates a new Window with a close button
; -------------------------------------------------
Function Gui_CreateWindow.GuiWidget(Px#, Py#, Sx#, Sy#, Label$)

    Win.GuiWidget = New GuiWidget
	
    Win\Px# = Px#
    Win\Py# = Py#
    Win\Sx# = Sx#
    Win\Sy# = Sy#

    Win\MinSx#	= Sx# / 2
    Win\MinSy#	= Sy# / 2
	
    Win\Label = label
	
    Win\WidgetType = Gui_WidgetTypeWindow
	
    Win\depth = Gui_Widget_HighestDepth
    Gui_Widget_HighestDepth = Gui_Widget_HighestDepth + 1
	
    Win\Active = True
    
    ; Add a "X" button to close the Window
    btn.GuiWidget = CreateButton(Win, Win\Sx# - 18, 2, 16, 16, "X")
    
    Return Win
	
End Function

; -----------------------------------------
; Internal : Function to update the Windows
; -----------------------------------------
Function Gui_RefreshWindow()
    ; -----------------------------------
    ; Profondeur maximale sous le curseur
    ; -----------------------------------
    Local Window_TopDepth = -1                 
    Local PotentialWindow.GuiWidget = Null     ; Fen�tre potentiellement sous le curseur
    Local absX#, absY#                         ; Variables pour �viter appels r�p�t�s

    ; R�initialise la fen�tre s�lectionn�e au d�but
    Gui_CurrentSelectedWindow = Null

    ; ----------------------------------------------------------------------------
    ; �tape 1 : D�termine quelle fen�tre est sous le curseur pour les interactions
    ; ----------------------------------------------------------------------------
    For Widget.GuiWidget = Each GuiWidget
        ; -------------------------------
        ; V�rifie uniquement les fen�tres
        ; -------------------------------
        If Widget\WidgetType = Gui_WidgetTypeWindow Then
            absX# = GetAbsoluteX(Widget)
            absY# = GetAbsoluteY(Widget)
			
            If Gui_TestZone(absX , absY , Widget\Sx , Widget\Sy , False , False) Then
			
                ; --------------------------------------------------
                ; Garde la fen�tre avec la profondeur la plus �lev�e
                ; --------------------------------------------------
                If Widget\depth > Window_TopDepth Then
                    PotentialWindow = Widget
                    Window_TopDepth = Widget\depth
                End If
				
            End If
			
        End If
    Next

    ; D�finit la fen�tre s�lectionn�e uniquement si une fen�tre est sous le curseur
    Gui_CurrentSelectedWindow = PotentialWindow

     ; ----------------------------------------
    ; �tape 2 : Gestion des clics (Mouse Down)
     ; ----------------------------------------
    If Gui_MouseClickLeft Then
	
        ; --------------------------------------------------------------
        ; D�placement ou redimensionnement de la fen�tre sous le curseur
        ; --------------------------------------------------------------
        If Gui_CurrentSelectedWindow <> Null Then
		
            absX# = GetAbsoluteX(Gui_CurrentSelectedWindow) ; R�utilis� plus loin
            absY# = GetAbsoluteY(Gui_CurrentSelectedWindow)
			
            ; --------------------------------------------------------------------------
            ; V�rifie si le clic est sur le gadget de redimensionnement (coin bas-droit)
            ; --------------------------------------------------------------------------
            If Gui_TestZone(absX + Gui_CurrentSelectedWindow\Sx - Gui_WindowSizeIcon , absY + Gui_CurrentSelectedWindow\Sy - Gui_WindowSizeIcon , Gui_WindowSizeIcon , Gui_WindowSizeIcon , False , False) Then
                Gui_CurrentSizedWindow = Gui_CurrentSelectedWindow
				
                resizeOffsetX = absX + Gui_CurrentSelectedWindow\Sx - Gui_MouseX
                resizeOffsetY = absY + Gui_CurrentSelectedWindow\Sy - Gui_MouseY
				
            ; -----------------------------------------------------------------
            ; V�rifie si le clic est dans la barre de titre pour le d�placement
            ; -----------------------------------------------------------------
            ElseIf Gui_MouseY% < absY + Gui_WindowTitleHeight Then
			
                Local closeButtonX# = Gui_CurrentSelectedWindow\Sx - 20
                Local closeButtonY# = 20
				
                ; -------------------------------------------------------------
                ; Active le d�placement si le clic n'est pas pr�s du bouton "X"
                ; -------------------------------------------------------------
                If Gui_TestZone(absX , absY , closeButtonX ,closeButtonY , False , False) Then
                    Gui_CurrentDraggedWindow = Gui_CurrentSelectedWindow
                    Gui_CurrentDraggedWindow\DragOffsetX = Gui_MouseX - absX
                    Gui_CurrentDraggedWindow\DragOffsetY = Gui_MouseY - absY
                End If
				
            End If
			
            ; --------------------------------------
            ; Met la fen�tre cliqu�e au premier plan
            ; --------------------------------------
            Local OldDepth = Gui_CurrentSelectedWindow\Depth
			
            For widget.GuiWidget = Each GuiWidget
                If widget\depth > OldDepth Then widget\depth = widget\depth - 1
            Next
			
            Gui_CurrentSelectedWindow\depth = Gui_Widget_HighestDepth - 1
            UpdateChildrenDepth(Gui_CurrentSelectedWindow)
			
        End If
    EndIf
    
    ; -----------------------------------------
    ; �tape 3 : Gestion continue du d�placement
    ; -----------------------------------------
    If Gui_CurrentDraggedWindow <> Null Then
        ; -----------------------------
        ; Tant que le clic est maintenu
        ; -----------------------------
        If Gui_MousePressLeft Then    
            Gui_CurrentDraggedWindow\Px = Gui_MouseX - Gui_CurrentDraggedWindow\DragOffsetX
            Gui_CurrentDraggedWindow\Py = Gui_MouseY - Gui_CurrentDraggedWindow\DragOffsetY
        Else
            Gui_CurrentDraggedWindow = Null
        End If
    End If

    ; -----------------------------------------------
    ; �tape 4 : Gestion continue du redimensionnement
    ; -----------------------------------------------
    If Gui_CurrentSizedWindow <> Null Then
	
        If Gui_MousePressLeft Then    					; Tant que le clic est maintenu
            absX# = GetAbsoluteX(Gui_CurrentSizedWindow)
            absY# = GetAbsoluteY(Gui_CurrentSizedWindow)
			
            newW# = Gui_MouseX% - absX + resizeOffsetX
            newH# = Gui_MouseY% - absY + resizeOffsetY
			
            If newW < Gui_CurrentSizedWindow\MinSx# Then newW = Gui_CurrentSizedWindow\MinSx#
            If newH < Gui_CurrentSizedWindow\MinSy# Then newH = Gui_CurrentSizedWindow\MinSy#
			
            Gui_CurrentSizedWindow\Sx = newW
            Gui_CurrentSizedWindow\Sy = newH
			
            ; -------------------------------------------------------
            ; Repositionne le bouton "X" dans le coin sup�rieur droit
            ; -------------------------------------------------------
            For i = 0 To Gui_CurrentSizedWindow\childCount - 1
                If Gui_CurrentSizedWindow\children[i] <> Null And Gui_CurrentSizedWindow\children[i]\label = "X" Then
                    Gui_CurrentSizedWindow\children[i]\Px = newW - 20
                End If
            Next
			
        Else
            Gui_CurrentSizedWindow = Null
        End If
		
    End If
End Function

; -----------------------------------------
; Internal : Function to redraw the Windows
; -----------------------------------------
Function Gui_RedrawWindow(Widget.GuiWidget)
	absX# = GetAbsoluteX(widget)    ; Position X absolue
	absY# = GetAbsoluteY(widget)    ; Position Y absolue

	; Gris pour le corps
	Gui_Rect(absX, absY, widget\Sx, widget\Sy , 1 , 100 , 100 , 100  , 0)

	; Bleu pour la barre de titre
	Gui_Rect(absX, absY, widget\Sx, Gui_WindowTitleHeight , 1 , 100 , 150 , 200  , 0)

	; Blanc pour le texte
	Gui_Text(absX + 5, absY + 2, widget\label , 255,255,255 , 1)

	; Resize gadget
	Gui_Rect(absX + widget\Sx - Gui_WindowSizeIcon, absY + widget\Sy - Gui_WindowSizeIcon, Gui_WindowSizeIcon, Gui_WindowSizeIcon , 1 , 100 , 150 , 200  , 0)
	
End Function
