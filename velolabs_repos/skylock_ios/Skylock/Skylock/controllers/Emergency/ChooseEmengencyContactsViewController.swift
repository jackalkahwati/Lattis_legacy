//
//  ChooseEmengencyContactsViewController.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 28/01/2017.
//  Copyright © 2017 Andre Green. All rights reserved.
//

import UIKit

protocol ChooseEmengencyContactsViewControllerDelegate: class {
    func saveSelected(contacts: [EllipseContact])
}

class ChooseEmengencyContactsViewController: ContactsViewController {
    weak var delegate: ChooseEmengencyContactsViewControllerDelegate?
    private let limit = 3
    private var saveBottomLayout: NSLayoutConstraint!
    fileprivate var selectedContacts: [EllipseContact] = []
    fileprivate var keyboardHeight: CGFloat = 0
    
    private let addText = "ADD EMERGENCY CONTACTS".localized()
    
    private let saveButton: UIButton = {
        let button = UIButton(type: .custom)
        button.translatesAutoresizingMaskIntoConstraints = false
        return button
    }()
    
    private let saveContainer: UIView = {
        let view = UIView()
        view.translatesAutoresizingMaskIntoConstraints = false
        view.backgroundColor = .slRobinsEgg
        return view
    }()
    
    private let saveLabel: UILabel = {
        let label = UILabel()
        label.translatesAutoresizingMaskIntoConstraints = false
        label.font = .systemFont(ofSize: 15)
        label.textAlignment = .center
        label.textColor = .white
        label.numberOfLines = 2
        return label
    }()
    
    init(contacts: [EllipseContact]) {
        selectedContacts = contacts
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        tableView.register(ChooseEllipseContactCell.self, forCellReuseIdentifier: cellIdentifire)
        title = "EMERGENCY CONTACTS".localized()
        
        view.addSubview(saveContainer)
        _ = saveContainer.constrainEqual(.leading, to: view, .leading)
        _ = saveContainer.constrainEqual(.trailing, to: view, .trailing)
        saveBottomLayout = saveContainer.constrainEqual(.bottom, to: view, .bottom)
        _ = saveContainer.constrainEqual(.height, to: nil, .height, constant: 60)
        
        saveContainer.addSubview(saveLabel)
        saveLabel.constrainEdges(to: saveContainer)
        
        saveContainer.addSubview(saveButton)
        saveButton.constrainEdges(to: saveContainer)
        saveButton.addTarget(self, action: #selector(saveAction), for: .touchUpInside)
        updateUI()
        
    }
    
    override func keyboardWillShow(notification: Notification) {
        super.keyboardWillShow(notification: notification)
        guard let info = notification.userInfo, let frameValue = info[UIKeyboardFrameEndUserInfoKey] as? NSValue else { return }
        let keyboardFrame = frameValue.cgRectValue
        keyboardHeight = keyboardFrame.height
        updateUI(animated: true)
    }
    
    override func keyboardWillDisappear(notification: Notification) {
        keyboardHeight = 0
        updateUI(animated: true)
    }
    
    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        super.tableView(tableView, didSelectRowAt: indexPath)
    }
    
    override func didSelect(contact: EllipseContact, at indexPath: IndexPath) {
        if let idx = selectedContacts.index(of: contact) {
            selectedContacts.remove(at: idx)
        } else if selectedContacts.count < limit {
            selectedContacts.append(contact)
        }
        tableView.reloadRows(at: [indexPath], with: .automatic)
        updateUI(animated: true)
    }
    
    private func updateUI(animated: Bool = false) {
        let height: CGFloat = (selectedContacts.isEmpty ? 0 : 60) + keyboardHeight
        var inset = tableView.contentInset
        inset.bottom = height
        tableView.contentInset = inset
        func updateButtonHeight() {
            saveBottomLayout.constant = selectedContacts.isEmpty ? 60 : -keyboardHeight
            saveContainer.alpha = selectedContacts.isEmpty ? 0 : 1
            view.layoutIfNeeded()
        }
        let title = NSAttributedString(string: addText, attributes: [NSFontAttributeName: UIFont.systemFont(ofSize: 15)])
        let text = NSMutableAttributedString(attributedString: title)
        if selectedContacts.count < limit {
            let baseString = limit - selectedContacts.count > 1 ? "\nYou can add %d more contacts".localized() : "\nYou can add %d more contact".localized()
            text.append(NSAttributedString(string: String(format: baseString, limit - selectedContacts.count), attributes: [NSFontAttributeName: UIFont.systemFont(ofSize: 12)]))
        }
        if selectedContacts.count == limit {
            let baseString = "\nYou can add no more contact"
            text.append(NSAttributedString(string: String(format: baseString, limit - selectedContacts.count), attributes: [NSFontAttributeName: UIFont.systemFont(ofSize: 12)]))
        }

        saveLabel.attributedText = text
        if animated {
            UIView.animate(withDuration: 0.3, animations: updateButtonHeight)
        } else {
            updateButtonHeight()
        }
    }
    
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = super.tableView(tableView, cellForRowAt: indexPath)  as! ChooseEllipseContactCell
        let contact = sections[indexPath.section].contacts[indexPath.row]
        cell.choosen = selectedContacts.contains(contact)
        return cell
    }
    
    @objc private func saveAction() {
        delegate?.saveSelected(contacts: selectedContacts)
        _ = navigationController?.popViewController(animated: true)
    }
}
