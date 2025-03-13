; ----------------------------------------
; Creates a new Window with a close button
; ----------------------------------------
Function Gui_CreateWindow.GuiWidget(Px#, Py#, Sx#, Sy#, Label$, Sizable = True , Modal = False)
    Win.GuiWidget = New GuiWidget
    
    Win\Px# = Px#
    Win\Py# = Py#
    Win\Sx# = Sx#
    Win\Sy# = Sy#
	
    Win\MinSx# = Sx# / 2
    Win\MinSy# = Sy# / 2
    
    Win\Label = Label
    Win\Modal = Modal
	Win\Sizable = Sizable
    
    Win\WidgetType = Gui_WidgetTypeWindow
    
	; ------------------------
    ; Gestion de la profondeur
	; ------------------------
    If Win\Modal Then
		
		; ----------------------------------------
        ; Place la fen�tre modale au sommet absolu
		; ----------------------------------------
        Win\Depth = Gui_Widget_HighestDepth + 1
        Gui_Widget_HighestDepth = Win\Depth + 1
		
    Else
		
        Win\Depth = Gui_Widget_HighestDepth
        Gui_Widget_HighestDepth = Gui_Widget_HighestDepth + 1
		
    End If
    
    Win\Active = True
    
    ; Ajout du bouton "X" pour fermer
    btn.GuiWidget = CreateButton(Win, Win\Sx# - 18, 2, 16, 16, "X")
    
    Return Win
End Function

; ------------------------------
; Function to update the Windows
; ------------------------------
Function Gui_RefreshWindow()
    
    ; ------------------------------------------	
    ; V�rifie s'il y a une fen�tre modale active
    ; ------------------------------------------
    Local HasModal = False
    Local ModalWindow.GuiWidget = Null
    
    For Widget.GuiWidget = Each GuiWidget
        If Widget\WidgetType = Gui_WidgetTypeWindow And Widget\Modal And Widget\Active Then
            HasModal = True
            ModalWindow = Widget
            Exit
        End If
    Next
    
    ; -----------------------------------
    ; Profondeur maximale sous le curseur
    ; -----------------------------------
    Local Window_TopDepth = -1                 
    Local ClickedWindow.GuiWidget = Null ; Fen�tre effectivement cliqu�e
    
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
            
            If Gui_TestZone(absX, absY, Widget\Sx, Widget\Sy, False, False) Then
                
                ; --------------------------------------------------
                ; Garde la fen�tre avec la profondeur la plus �lev�e
                ; --------------------------------------------------
                If (HasModal And Widget = ModalWindow) Or (Not HasModal) Then
                    
                    If Widget\Depth > Window_TopDepth Then
                        Gui_CurrentSelectedWindow = Widget ; D�finit la fen�tre active
                        Window_TopDepth = Widget\Depth
                    End If
                    
                End If
                
            End If
            
        End If
        
    Next
    
    ; ----------------------------------------
    ; �tape 2 : Gestion des clics (Mouse Down)
    ; ----------------------------------------
    If Gui_MouseClickLeft Then
        
        ; --------------------------------------------------------------
        ; D�placement ou redimensionnement de la fen�tre sous le curseur
        ; --------------------------------------------------------------
        If Gui_CurrentSelectedWindow <> Null Then
            
            absX# = GetAbsoluteX(Gui_CurrentSelectedWindow)
            absY# = GetAbsoluteY(Gui_CurrentSelectedWindow)
            
            ; -------------------------------------------------------------
            ; V�rifie si le clic initial est dans la fen�tre (Click = True)
            ; -------------------------------------------------------------
            If Gui_TestZone(absX, absY, Gui_CurrentSelectedWindow\Sx, Gui_CurrentSelectedWindow\Sy, True, False) Then
                
                ClickedWindow = Gui_CurrentSelectedWindow
                
                ; --------------------------------------------------------------------------
                ; V�rifie si le clic est sur le gadget de redimensionnement (coin bas-droit)
                ; --------------------------------------------------------------------------
                If Gui_CurrentSelectedWindow\Sizable Then ; Ajout de la condition Sizable
                    If Gui_TestZone(absX + Gui_CurrentSelectedWindow\Sx - Gui_WindowSizeIcon, absY + Gui_CurrentSelectedWindow\Sy - Gui_WindowSizeIcon, Gui_WindowSizeIcon, Gui_WindowSizeIcon, True, False) Then
						
                        Gui_CurrentSizedWindow = Gui_CurrentSelectedWindow      									; Active le redimensionnement
                        Gui_CurrentSelectedWindow\SizeOffsetX# = absX + Gui_CurrentSelectedWindow\Sx - Gui_MouseX 	; Calcule le d�calage X
                        Gui_CurrentSelectedWindow\SizeOffsetY# = absY + Gui_CurrentSelectedWindow\Sy - Gui_MouseY 	; Calcule le d�calage Y
						
                    End If
                End If
                
                ; -----------------------------------------------------------------
                ; V�rifie si le clic est dans la barre de titre pour le d�placement
                ; -----------------------------------------------------------------
                If Gui_MouseClickLeft_Y < absY + 20 Then
                    
                    Local closeButtonX# = Gui_CurrentSelectedWindow\Sx - 20   									; Position X du bouton "X"
                    Local closeButtonY# = 20 					 												; Taille Y du bouton "X"
                    
                    ; -------------------------------------------------------------
                    ; Active le d�placement si le clic n'est pas pr�s du bouton "X"
                    ; -------------------------------------------------------------
                    If Gui_TestZone(absX + closeButtonX, absY, 16, 16, True, False) = False Then 				; �vite le bouton "X"
                        Gui_CurrentDraggedWindow = Gui_CurrentSelectedWindow      								; Active le d�placement
                        Gui_CurrentDraggedWindow\DragOffsetX = Gui_MouseX - absX 
                        Gui_CurrentDraggedWindow\DragOffsetY = Gui_MouseY - absY
                    End If
                    
                End If
                
                ; --------------------------------------
                ; Met la fen�tre cliqu�e au premier plan
                ; --------------------------------------
                
                If Not ClickedWindow\Modal Then ; Ne change la profondeur que pour les non modales
                    Local OldDepth = ClickedWindow\Depth
                    
                    For widget.GuiWidget = Each GuiWidget
                        If widget\Depth > OldDepth And widget\Modal = False Then
                            widget\Depth = widget\Depth - 1 ; D�cale les autres sauf modales
                        End If
                    Next
                    
                    ClickedWindow\Depth = Gui_Widget_HighestDepth - 1
                    
                    If HasModal Then ClickedWindow\Depth = ModalWindow\Depth - 1 	; Sous la modale
                    
                    UpdateChildrenDepth(ClickedWindow)                  			; Met � jour la profondeur des enfants
                End If
                
            Else
                
                ; ------------------------------------------------------------
                ; R�initialise la current window si clic hors de toute fen�tre
                ; ------------------------------------------------------------
                Gui_CurrentSelectedWindow = Null
                
            End If
        Else
            
            ; -------------------------
            ; Aucun clic valide d�tect�
            ; -------------------------
            Gui_CurrentSelectedWindow = Null 
            
        End If
        
    EndIf
    
    ; ----------------------------------------------
    ; �tape 3 : Permet le d�placement m�me si modale
    ; ----------------------------------------------
    If Gui_CurrentDraggedWindow <> Null Then 
        
        If Gui_MousePressLeft Then
            
            Gui_CurrentDraggedWindow\Px = Gui_MouseX - Gui_CurrentDraggedWindow\DragOffsetX ; Met � jour la position X
            Gui_CurrentDraggedWindow\Py = Gui_MouseY - Gui_CurrentDraggedWindow\DragOffsetY ; Met � jour la position Y
            
        Else
            
            ; --------------------------------
            ; Arr�te le d�placement si rel�ch�
            ; --------------------------------
            Gui_CurrentDraggedWindow = Null   
            
        End If
        
    End If
    
    ; ----------------------------------------------------
    ; �tape 4 : Permet le redimensionnement m�me si modale
    ; ----------------------------------------------------
    If Gui_CurrentSizedWindow <> Null  Then 
        
        If Gui_MousePressLeft And Gui_CurrentSizedWindow\Sizable = True Then ; Ajout de la condition Sizable    											
            
            absX# = GetAbsoluteX(Gui_CurrentSizedWindow)
            absY# = GetAbsoluteY(Gui_CurrentSizedWindow)
            
            newW# = Gui_MouseX% - absX + Gui_CurrentSizedWindow\SizeOffsetX#   									; Calcule la nouvelle largeur
            newH# = Gui_MouseY% - absY + Gui_CurrentSizedWindow\SizeOffsetY#  									; Calcule la nouvelle hauteur
            
            If newW < Gui_CurrentSizedWindow\MinSx# Then newW = Gui_CurrentSizedWindow\MinSx#           		; Limite minimale de largeur
            If newH < Gui_CurrentSizedWindow\MinSy# Then newH = Gui_CurrentSizedWindow\MinSy#             		; Limite minimale de hauteur
            
            Gui_CurrentSizedWindow\Sx = newW                													; Applique la nouvelle largeur
            Gui_CurrentSizedWindow\Sy = newH                													; Applique la nouvelle hauteur
            
            ; -------------------------------------------------------
            ; Repositionne le bouton "X" dans le coin sup�rieur droit
            ; -------------------------------------------------------
            For i = 0 To Gui_CurrentSizedWindow\ChildCount - 1
                If Gui_CurrentSizedWindow\Children[i] <> Null And Gui_CurrentSizedWindow\Children[i]\Label = "X" Then
                    Gui_CurrentSizedWindow\Children[i]\Px = newW - 20
                End If
            Next
            
        Else
            
            ; --------------------------------------
            ; Arr�te le redimensionnement si rel�ch�
            ; --------------------------------------
            Gui_CurrentSizedWindow = Null   
            
        End If
        
    End If
    
End Function

; ------------------------------------
; Function to redraw the Windows
; ------------------------------------
; ------------------------------------
; Function to redraw the Windows
; ------------------------------------
Function Gui_RedrawWindow(Widget.GuiWidget)
    absX# = GetAbsoluteX(Widget)    ; Position X absolue
    absY# = GetAbsoluteY(Widget)    ; Position Y absolue
	
    ; Gris pour le corps
    Gui_Rect(absX, absY, Widget\Sx, Widget\Sy, 1, 100, 100, 100, 0)
	
    ; Couleur de la barre de titre : deux �tats seulement
    If Widget\Depth = Gui_Widget_HighestDepth - 1 Then
        ; Rouge si s�lectionn�e (au premier plan)
        Gui_Rect(absX, absY, Widget\Sx, 20, 1, 150, 50, 50, 0)
    Else
        ; Gris fonc� si non s�lectionn�e (en arri�re-plan)
        Gui_Rect(absX, absY, Widget\Sx, 20, 1, 50, 50, 50, 0)
    End If
    
    ; Blanc pour le texte
    Gui_Text(absX + 5, absY + 2, Widget\Label, 255, 255, 255, 1)
    
    ; Gris clair pour le gadget de redimensionnement (affich� seulement si Sizable)
    If Widget\Sizable Then
        Gui_Rect(absX + Widget\Sx - Gui_WindowSizeIcon, absY + Widget\Sy - Gui_WindowSizeIcon, Gui_WindowSizeIcon, Gui_WindowSizeIcon, 1, 100, 160, 100, 0)
    End If
End Function
;~IDEal Editor Parameters:
;~C#Blitz3D