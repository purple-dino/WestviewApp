import UIKit
import SwiftUI
import WestVUE
import ActivityKit

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
                .ignoresSafeArea() // Compose has own keyboard handler
    }
//    
//    func startActivity() {
//        let attributes = CountdownAttributes(startTime: Date(), endTime: Date().advanced(by: TimeInterval(60)))
//        let state = CountdownAttributes.ContentState()
//        
//        if #available(iOS 16.1, *) {
//            try? Activity<CountdownAttributes>.request(attributes: attributes, contentState: state, pushType: nil)
//        } else {
//            // Fallback on earlier versions
//        }
//    }
}
