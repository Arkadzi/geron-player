package me.arkadiy.geronplayer.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.assist.ImageSize;

import me.arkadiy.geronplayer.MainActivity;
import me.arkadiy.geronplayer.R;
import me.arkadiy.geronplayer.statics.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class ImageFragment extends Fragment {


    public static final String KEY = "image";
    private ImageSize imageSize = new ImageSize(500, 500);
    public ImageFragment() {
        // Required empty public constructor
    }

    public static Fragment newInstance(long albumID) {
        ImageFragment fragment = new ImageFragment();
        Bundle args = new Bundle();
        args.putLong(KEY, albumID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_image, container, false);
        if (getArguments() != null) {
            long id = getArguments().getLong(KEY, -1);
            String uri = Utils.getArtworks(id).toString();
//            movingView = view.findViewById(R.id.moving_view);
//            Log.e("ImageFragment", movingView.getY() + " " + movingView.getTranslationY());
            ImageView imageView = (ImageView) view.findViewById(R.id.cover_art);
//            set = new AnimatorSet();
            view.setOnClickListener(new View.OnClickListener() {
                //                public float y;
//
                @Override
                public void onClick(View v) {
                    MainActivity activity = (MainActivity) getActivity();
                    if (activity != null) {
                        activity.animateHighlight();
                    }
                }
            });

//            movingView.setOnTouchListener(new View.OnTouchListener() {
//                public float downY;
//                public float startY;
//
//                @Override
//                public boolean onTouch(View v, MotionEvent event) {
//                    int eid = event.getAction();
//                    switch (eid) {
//                        case MotionEvent.ACTION_MOVE:
//                            float mv = event.getY() - downY;
//                            movingView.setY((int)(startY + mv));
//                            this.startY = movingView.getY();
//                            break;
//                        case MotionEvent.ACTION_DOWN :
//                            this.downY = event.getY();
//                            this.startY = movingView.getY();
//                            break;
//                        case MotionEvent.ACTION_UP :
//                            // Nothing have to do
//                            break;
//                        default :
//                            break;
//                    }
//                    return true;
//                }
//            });
            MainActivity.imageLoader.displayImage(uri, imageView, MainActivity.options);
//            Picasso.with(getActivity()).load(uri).into(imageView);
        }
        return view;
    }
}
