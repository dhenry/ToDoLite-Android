package com.couchbase.todolite.ui.tasks;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.couchbase.lite.Attachment;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.SavedRevision;
import com.couchbase.lite.util.Log;
import com.couchbase.todolite.Application;
import com.couchbase.todolite.ImageViewActivity;
import com.couchbase.todolite.R;
import com.couchbase.todolite.document.Task;
import com.couchbase.todolite.helper.ImageHelper;
import com.couchbase.todolite.helper.LiveQueryBaseAdapter;

import java.io.ByteArrayOutputStream;

class TasksAdapter extends LiveQueryBaseAdapter {

    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int REQUEST_CHOOSE_PHOTO = 2;

    private static final int THUMBNAIL_SIZE_PX = 150;

    private Context context;

    public TasksAdapter(Context context, LiveQuery query) {
        super(context, query);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) parent.getContext().
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.view_task, null);
        }

        final Document task = (Document) getItem(position);

        if (task == null || task.getCurrentRevision() == null) {
            return convertView;
        }

        Bitmap thumbnail = null;
        java.util.List<Attachment> attachments = task.getCurrentRevision().getAttachments();
        if (attachments != null && attachments.size() > 0) {
            Attachment attachment = attachments.get(0);
            try {
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(attachment.getContent(), null, options);
                options.inSampleSize = ImageHelper.calculateInSampleSize(
                        options, THUMBNAIL_SIZE_PX, THUMBNAIL_SIZE_PX);
                attachment.getContent().close();

                // Need to get a new attachment again as the FileInputStream
                // doesn't support mark and reset.
                attachments = task.getCurrentRevision().getAttachments();
                attachment = attachments.get(0);
                options.inJustDecodeBounds = false;
                Bitmap bitmap = BitmapFactory.decodeStream(attachment.getContent(), null, options);
                int w = bitmap.getWidth();
                int h = bitmap.getHeight();
                thumbnail = ThumbnailUtils.extractThumbnail(bitmap, THUMBNAIL_SIZE_PX, THUMBNAIL_SIZE_PX);
                attachment.getContent().close();
            } catch (Exception e) {
                Log.e(Application.TAG, "Cannot decode the attached image", e);
            }
        }

        ImageView imageView = (ImageView) convertView.findViewById(R.id.image);
        if (thumbnail != null) {
            imageView.setImageBitmap(thumbnail);
        } else {
            imageView.setImageDrawable(this.context.getDrawable(R.drawable.ic_camera_light));
        }

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                java.util.List<Attachment> attachments =
                        task.getCurrentRevision().getAttachments();
                if (attachments != null && attachments.size() > 0) {
                    Attachment attachment = attachments.get(0);
                    try {
                        Bitmap displayImage = BitmapFactory.decodeStream(attachment.getContent());
                        dispatchImageViewIntent(displayImage);
                        attachment.getContent().close();
                    } catch (Exception e) {
                        Log.e(Application.TAG, "Cannot decode the attached image", e);
                    }
                } else {
//                    attachImage(task);
                }
            }
        });

        TextView text = (TextView) convertView.findViewById(R.id.text);
        text.setText((String) task.getProperty("title"));

        final CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checked);
        Boolean checkedProperty = (Boolean) task.getProperty("checked");
        boolean checked = checkedProperty != null ? checkedProperty.booleanValue() : false;
        checkBox.setChecked(checked);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Task.updateCheckedStatus(task, checkBox.isChecked());
                } catch (CouchbaseLiteException e) {
                    Log.e(Application.TAG, "Cannot update checked status", e);
                }
            }
        });


        /*
        If there are conflicting revisions, show a conflict icon.
        getConflictingRevisions always returns the current revision
        so we must check for size > 1.
         */
        java.util.List<SavedRevision> conflicts = null;
        try {
            conflicts = task.getConflictingRevisions();
        } catch (CouchbaseLiteException e) {

        }
        ImageView conflictIcon = (ImageView) convertView.findViewById(R.id.conflict_icon);
        conflictIcon.setVisibility(View.INVISIBLE);
        if (conflicts.size() > 1) {
            conflictIcon.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    /*
    This method is called
     */
    private void dispatchImageViewIntent(Bitmap image) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 50, stream);
        byte[] byteArray = stream.toByteArray();

        long l = byteArray.length;

        Intent intent = new Intent(context, ImageViewActivity.class);
        intent.putExtra(ImageViewActivity.INTENT_IMAGE, byteArray);
        context.startActivity(intent);
    }



}