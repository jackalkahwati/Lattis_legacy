//
//  AddNotesViewController.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 01/05/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

class AddNotesViewController: UIViewController {
    @IBOutlet weak var textView: UITextView!
    @IBOutlet var notesToolbar: UIToolbar!
    
    var text: String = ""
    var saveNote: (String) -> () = {_ in}

    override func viewDidLoad() {
        super.viewDidLoad()

        textView.inputAccessoryView = notesToolbar
        textView.text = text
    }
    
    override var prefersStatusBarHidden: Bool {
        return true
    }
    
    @IBAction func save(_ sender: Any) {
        saveNote(textView.text)
        close(sender)
    }
    
    @IBAction func close(_ sender: Any) {
        dismiss(animated: true, completion: nil)
    }
}
