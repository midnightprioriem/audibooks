# Audibooks

[![Travis](https://img.shields.io/travis/rust-lang/rust.svg)]()
[![JitPack](https://img.shields.io/jitpack/v/jitpack/maven-simple.svg)]()

_Android application for listening to .mp3 audiobooks_


<img src="https://raw.githubusercontent.com/zachhannum/audibooks/master/Images/book_chooser.png"
       alt="Audibooks Book Chooser List View" width="200"/>
       <img src="https://raw.githubusercontent.com/zachhannum/audibooks/master/Images/book_chooser_grid.png"
       alt="Audibooks Book Chooser List View" width="200"/>
       <img src="https://raw.githubusercontent.com/zachhannum/audibooks/master/Images/play_book3.png"
       alt="Audibooks Book Chooser List View" width="200"/>

## Features
- Persistance between listening sessions. Your place for each book is saved when you exit the app.
- List view and Grid view library
- Streamlined sliding panel player
- Sort library by title/author

## Download

You can download the .apk for installation from the latest [release](https://github.com/zachhannum/audibooks/releases)

## Getting Started

This App is built to playback audiobooks with an .mp3 format. In order to use Audibooks, you must have a copy of the audibook you want to listen to as either a single .mp3, or a series of chapterized .mp3's. In the case of chapterized .mp3's, _all_ metadata must be formatted correctly for Audibooks to interpret them. Formatting is as follows:

| Metadata        | Audibooks |    
| ------------- |:-------------:| 
| track\*      | chapter # | 
| album\*\*      | book title      |
| artist\*\* | book author   |

\*track # must be consistent and in descending order in chapterized books

\*\*album and artist _must_ be the same for all .mp3s in chapterized books

#### Path

Audibooks relies on a specific file setup to organize books and chapters. Please organize your books as follows:

`/Audiobooks/BOOK_TITLE/CHAPTER_X.mp3`

Naming convention is unimportant, just the way the folders are organized.

#### Book Covers

Audibooks will use any image file placed in the `BOOK_TITLE` folder along with the .mp3s. Audibooks _will not_ use any metadata album artwork.

##### Enjoy!

This app was created as one of my first Android applications in 2015, mostly built for my own enjoyment. I still use it to this day. If anyone else happens across this app and decides to use it, I would love to hear about your experience with the app!

## Dependencies
- [AndroidSlidingUpPanel](https://github.com/umano/AndroidSlidingUpPanel)
- [picasso](https://github.com/square/picasso)
- [NoNonsense-FilePicker](https://github.com/spacecowboy/NoNonsense-FilePicker)
- [vector-compat](https://github.com/wnafee/vector-compat)
- [material-menu](https://github.com/balysv/material-menu)
- [Blurry](https://github.com/wasabeef/Blurry)


