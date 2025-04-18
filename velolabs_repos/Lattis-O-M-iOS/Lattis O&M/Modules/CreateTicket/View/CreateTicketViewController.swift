//
//  CreateTicketCreateTicketViewController.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 28/04/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

class CreateTicketViewController: ViewController {
    
    @IBOutlet weak var nameLabel: UILabel!
    @IBOutlet weak var catField: UITextField!
    @IBOutlet weak var photoView: UIImageView!
    @IBOutlet weak var notesLabel: UILabel!
    @IBOutlet weak var noPhotoLayout: NSLayoutConstraint!
    @IBOutlet weak var photoLayout: NSLayoutConstraint!
    @IBOutlet var pickerToolbar: UIToolbar!
    fileprivate let picker = UIPickerView()
    fileprivate var categories: [Ticket.Category] = [.damage_reported, .service_due, .parking_outside_geofence]
    
    var interactor: CreateTicketInteractorInput!

    override func viewDidLoad() {
        super.viewDidLoad()

        title = "create_ticket_title".localized().uppercased()
        navigationItem.leftBarButtonItem = .back(target: self, action: #selector(back))
        let saveBtn = UIBarButtonItem(title: "general_btn_save".localized().uppercased(), style: .plain, target: self, action: #selector(save))
        saveBtn.isEnabled = false
        navigationItem.rightBarButtonItem = saveBtn
        
        catField.delegate = self
        catField.inputView = picker
        catField.inputAccessoryView = pickerToolbar
        
        picker.delegate = self
        picker.dataSource = self
        
        interactor.viewLoaded()
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if let notes = segue.destination as? AddNotesViewController {
            notes.text = notesLabel.text!
            notes.saveNote = { [unowned self] text in
                self.notesLabel.text = text
                self.view.layoutIfNeeded()
                self.validate()
            }
        }
    }
    
    @objc private func back() {
        _ = navigationController?.popViewController(animated: true)
    }
    
    @objc private func save() {
        interactor.createTicket(note: notesLabel.text!)
    }
    
    @IBAction func categoryDone(_ sender: Any) {
        view.endEditing(true)
        validate()
    }
    
    @IBAction func takePhoto(_ sender: Any) {
        let picker = UIImagePickerController()
        picker.sourceType = TARGET_OS_SIMULATOR != 0 ? .photoLibrary : .camera
        picker.delegate = self
        present(picker, animated: true, completion: nil)
    }
    
    fileprivate func validate() {
        navigationItem.rightBarButtonItem?.isEnabled = catField.text!.isEmpty == false && notesLabel.text!.isEmpty == false
    }
}

extension CreateTicketViewController: CreateTicketInteractorOutput {
    func show(bike: Bike) {
        nameLabel.text = bike.name
    }
    
    func showSuccess() {
        let alert = ErrorAlertView.alert(title: "create_ticket_success_title".localized(), subtitle: "create_ticket_success_text".localized())
        alert.action = { _ = self.navigationController?.popToRootViewController(animated: true) }
        stopLoading {
            alert.show()
        }
    }
}

extension CreateTicketViewController: UITextFieldDelegate {
    func textFieldDidBeginEditing(_ textField: UITextField) {
        let category = categories[picker.selectedRow(inComponent: 0)]
        interactor.select(category: category)
        textField.text = category.displayTitle
        validate()
    }
}

extension CreateTicketViewController: UIPickerViewDelegate, UIPickerViewDataSource {
    func numberOfComponents(in pickerView: UIPickerView) -> Int {
        return 1
    }
    
    func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
        return categories.count
    }
    
    func pickerView(_ pickerView: UIPickerView, titleForRow row: Int, forComponent component: Int) -> String? {
        return categories[row].displayTitle
    }
    
    func pickerView(_ pickerView: UIPickerView, didSelectRow row: Int, inComponent component: Int) {
        catField.text = categories[row].displayTitle
        interactor.select(category: categories[row])
    }
}

extension CreateTicketViewController: UIImagePickerControllerDelegate, UINavigationControllerDelegate {
    func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]) {
// Local variable inserted by Swift 4.2 migrator.
let info = convertFromUIImagePickerControllerInfoKeyDictionary(info)

        guard let image = info[convertFromUIImagePickerControllerInfoKey(UIImagePickerController.InfoKey.originalImage)] as? UIImage else { return }
        photoView.image = image
        photoView.isHidden = false
        
        interactor.photo = image.resizeImageWith(newSize: .side(800))
        
        noPhotoLayout.priority = UILayoutPriority(rawValue: 800)
        photoLayout.priority = UILayoutPriority(rawValue: 900)
        view.layoutIfNeeded()
        validate()
        dismiss(animated: true, completion: nil)
    }
    
    func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
        dismiss(animated: true, completion: nil)
    }
}

// Helper function inserted by Swift 4.2 migrator.
fileprivate func convertFromUIImagePickerControllerInfoKeyDictionary(_ input: [UIImagePickerController.InfoKey: Any]) -> [String: Any] {
	return Dictionary(uniqueKeysWithValues: input.map {key, value in (key.rawValue, value)})
}

// Helper function inserted by Swift 4.2 migrator.
fileprivate func convertFromUIImagePickerControllerInfoKey(_ input: UIImagePickerController.InfoKey) -> String {
	return input.rawValue
}
