Hey! :)

I'm actually working on a new GUI for blitz3D / Blitzmax / Blitzplus, the kernel of this GUI is 
good and fast, but not perfect... So i'm actually reworking on a new simple kernel of GUI with
Event system. It's a very simple but robust base that you can improve. You can actually highlight
the button, get the event pressed, and clicked. The window Z order work well.

I deliberately kept the code graphically simple, so as not to overload it. But I hope that this 
example will make you want to code your own GUI :) ? I put a maximum of comments to help you 
understand how all this crap works :)

Because making a GUI is really not easy, there are traps at each stage of the creation... 
But if you already correctly manage the zorder of the windows and the clicks on the buttons, 
that's 90% of the work done.

The rest of the traps will be for example the creation of combo boxes! And yes imagine you 
have a button under the drop-down menu of a combo? how do you make sure you don't click on
the button below it, when the combo menu is unfolded?

In short, you have millions of traps like that... The first screenshot corresponds to the 
simplified kernel code, and the second screenshot corresponds to the current status of the GUI
I was talking about at the beginning of the topic. It is very robust, fast, and convertible 
under blitzmax without problem...

I don't know if i should release this kind of library as freeware or shareware, because I have
doubts about the number of people that it interests :) Who still programs in blitz professionally?

I have no idea :) But here it is, I always loved making GUIs :) When I was young I was already
programming GUIs on Amiga in GFa Basic.

![Screenshoot](https://github.com/user-attachments/assets/72e84586-7782-4e35-aa1a-7b1dc3436a0e)

The GUI i talked previously:
![Screenshoot_C](https://github.com/user-attachments/assets/8ddd2cc7-3da3-4703-b5cd-8baa03ffdd02)

![Screenshoot_B](https://github.com/user-attachments/assets/084f2741-b184-4c16-bf18-2560c15b9793)

