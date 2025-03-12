; ----------------------------------------
; Creates a new Window with a close button
; ----------------------------------------
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

; ------------------------------
; Function to update the Windows
; ------------------------------
Function Gui_RefreshWindow()
    Local Window_TopDepth = -1
    Local clickedWindow.GuiWidget = Null ; Pour stocker la fenêtre cliquée

    ; Étape 1 : Détermine quelle fenêtre est sous le curseur
    For Widget.GuiWidget = Each GuiWidget
        If Widget\WidgetType = Gui_WidgetTypeWindow Then
            absX# = GetAbsoluteX(Widget)
            absY# = GetAbsoluteY(Widget)
            
            Widget\Hovered = False
            
            If Gui_TestZone(absX, absY, Widget\Sx, Widget\Sy, False, False) Then
                If Widget\depth > Window_TopDepth Then
                    Gui_CurrentSelectedWindow = Widget
                    Window_TopDepth = Widget\depth
                End If
            End If
        End If
    Next

    ; Étape 2 : Gestion des clics
    If Gui_MouseClickLeft Then
        ; Vérifie si le clic est dans la fenêtre sélectionnée
        If Gui_CurrentSelectedWindow <> Null Then
            absX# = GetAbsoluteX(Gui_CurrentSelectedWindow)
            absY# = GetAbsoluteY(Gui_CurrentSelectedWindow)
            
            ; Confirme que le clic est dans la zone avant toute action
            If Gui_TestZone(absX, absY, Gui_CurrentSelectedWindow\Sx, Gui_CurrentSelectedWindow\Sy, False, False) Then
                clickedWindow = Gui_CurrentSelectedWindow
                Gui_CurrentSelectedWindow\Hovered = True
                
                ; Gestion du redimensionnement
                If Gui_TestZone(absX + Gui_CurrentSelectedWindow\Sx - Gui_WindowSizeIcon, absY + Gui_CurrentSelectedWindow\Sy - Gui_WindowSizeIcon, Gui_WindowSizeIcon, Gui_WindowSizeIcon, False, False) Then
                    Gui_CurrentSizedWindow = Gui_CurrentSelectedWindow
                    Gui_CurrentSelectedWindow\SizeOffsetX# = absX + Gui_CurrentSelectedWindow\Sx - Gui_MouseX
                    Gui_CurrentSelectedWindow\SizeOffsetY# = absY + Gui_CurrentSelectedWindow\Sy - Gui_MouseY
                ; Gestion du déplacement
                ElseIf Gui_MouseY% < absY + 20 Then
                    Local closeButtonX# = Gui_CurrentSelectedWindow\Sx - 20
                    Local closeButtonY# = 20
                    If Gui_TestZone(absX, absY, closeButtonX, closeButtonY, False, False) Then
                        Gui_CurrentDraggedWindow = Gui_CurrentSelectedWindow
                        Gui_CurrentDraggedWindow\DragOffsetX = Gui_MouseX - absX
                        Gui_CurrentDraggedWindow\DragOffsetY = Gui_MouseY - absY
                    End If
                End If
                
                ; Mise au premier plan uniquement si clic valide
                Local OldDepth = clickedWindow\Depth
                For widget.GuiWidget = Each GuiWidget
                    If widget\depth > OldDepth Then widget\depth = widget\depth - 1
                Next
                clickedWindow\depth = Gui_Widget_HighestDepth - 1
                UpdateChildrenDepth(clickedWindow)
            Else
                ; Réinitialise si le clic est hors de la fenêtre
                Gui_CurrentSelectedWindow = Null
            End If
        End If
    End If

    ; Étape 3 et 4 : Déplacement et redimensionnement (inchangés)
    If Gui_CurrentDraggedWindow <> Null Then
        If Gui_MousePressLeft Then
            Gui_CurrentDraggedWindow\Px = Gui_MouseX - Gui_CurrentDraggedWindow\DragOffsetX
            Gui_CurrentDraggedWindow\Py = Gui_MouseY - Gui_CurrentDraggedWindow\DragOffsetY
        Else
            Gui_CurrentDraggedWindow = Null
        End If
    End If

    If Gui_CurrentSizedWindow <> Null Then
        If Gui_MousePressLeft Then
            absX# = GetAbsoluteX(Gui_CurrentSizedWindow)
            absY# = GetAbsoluteY(Gui_CurrentSizedWindow)
            newW# = Gui_MouseX% - absX + Gui_CurrentSizedWindow\SizeOffsetX#
            newH# = Gui_MouseY% - absY + Gui_CurrentSizedWindow\SizeOffsetY#
            If newW < Gui_CurrentSizedWindow\MinSx# Then newW = Gui_CurrentSizedWindow\MinSx#
            If newH < Gui_CurrentSizedWindow\MinSy# Then newH = Gui_CurrentSizedWindow\MinSy#
            Gui_CurrentSizedWindow\Sx = newW
            Gui_CurrentSizedWindow\Sy = newH
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

; ------------------------------------
; Function to redraw the Windows
; ------------------------------------
Function Gui_RedrawWindow(Widget.GuiWidget)
    absX# = GetAbsoluteX(widget)    ; Position X absolue
    absY# = GetAbsoluteY(widget)    ; Position Y absolue

    ; Gris pour le corps
    Gui_Rect(absX, absY, widget\Sx, widget\Sy , 1 , 100 , 100 , 100  , 0)

    ; Bleu pour la barre de titre
    If Widget\Depth = Gui_Widget_HighestDepth - 1 Then
        If Widget\Hovered = False
            Gui_Rect(absX, absY, widget\Sx, 20 , 1 , 50 , 100 , 150  , 0)
        EndIf
    Else
        Gui_Rect(absX, absY, widget\Sx, 20 , 1 , 150 , 100 , 100  , 0)
    EndIf
    
    ; Blanc pour le texte
    Gui_Text(absX + 5, absY + 2, widget\label , 255,255,255 , 1)
    
    Color 150,150,150           ; Gris clair pour le gadget de redimensionnement
    Rect absX + widget\Sx - Gui_WindowSizeIcon, absY + widget\Sy - Gui_WindowSizeIcon, Gui_WindowSizeIcon, Gui_WindowSizeIcon, 1
End Function